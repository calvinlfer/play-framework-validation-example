import java.time.Clock
import javax.inject.{Named, Singleton}

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Inject, Provider}
import play.api.{Configuration, Environment, Logger}
import services.data._

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)

    bind(classOf[AmazonDynamoDBClient]).toProvider(classOf[DynamoDBClientProvider]).asEagerSingleton()

    // Use the in-memory implementation for PersonsDAO
    bind(classOf[PersonsRepository]).to(classOf[InMemoryPersonsRepository]).asEagerSingleton()
  }
}

class DynamoDBClientProvider @Inject()(configuration: Configuration) extends Provider[AmazonDynamoDBClient] {
  val log = Logger("DynamoDB configuration")

  override def get(): AmazonDynamoDBClient = {
    val optEndpoint = configuration.getString("dynamodb.endpoint")
    val optAccessKey = configuration.getString("dynamodb.aws-access-key-id")
    val optSecretKey = configuration.getString("dynamodb.aws-secret-access-key")
    val optRegion = configuration.getString("dynamodb.region").map(r => Regions.fromName(r))

    // Dear God, please forgive me for horrible mutation
    val dynamoClient =
      if (optAccessKey.isDefined && optSecretKey.isDefined) {
        new AmazonDynamoDBClient(new BasicAWSCredentials(optAccessKey.get, optSecretKey.get))
      }
      else {
        new AmazonDynamoDBClient()
      }

    if (optRegion.isDefined) dynamoClient.withRegion(optRegion.get)
    if (optEndpoint.isDefined) dynamoClient.withEndpoint(optEndpoint.get)

    log.info("DynamoDB client configured with:")
    optRegion.foreach(region => log.info(s"AWS Region: ${region.toString.toLowerCase}"))
    optEndpoint.foreach(endpoint => log.info(s"Endpoint: $endpoint"))
    optAccessKey.foreach(_ => log.info("AWS Access Key ID has been provided"))
    optSecretKey.foreach(_ => log.info("AWS Secret Access Key has been provided"))

    dynamoClient
  }
}
