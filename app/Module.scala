import java.io.{File, PrintWriter}

import com.google.inject.{AbstractModule, Inject, Provider}
import java.time.Clock
import javax.inject.{Named, Singleton}

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.google.inject.name.Names
import play.api.{Configuration, Environment, Logger}
import services.data._

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)

    // Use the in-memory implementation for PersonsDAO
    bind(classOf[PersonsRepository]).to(classOf[InMemoryPersonsRepository]).asEagerSingleton()
    bind(classOf[AmazonDynamoDBClient])
      .annotatedWith(Names.named("DynamoClient"))
      .toProvider(classOf[DynamoDBClientProvider])
      .asEagerSingleton()
  }
}

@Singleton
@Named("DynamoClient")
class DynamoDBClientProvider @Inject() (configuration: Configuration) extends Provider[AmazonDynamoDBClient] {
  val log = Logger("DynamoDB configuration")
  override def get(): AmazonDynamoDBClient = {
    val region = configuration.getString("dynamodb.region").map(r => Regions.fromName(r)).getOrElse(Regions.US_WEST_1)
    val endpoint = configuration.getString("dynamodb.endpoint")
    val accessKey = configuration.getString("dynamodb.aws-access-key-id")
    val secretKey = configuration.getString("dynamodb.aws-secret-access-key")
    val dynamoClient =
      if (accessKey.isDefined && secretKey.isDefined) {
        new AmazonDynamoDBClient(new BasicAWSCredentials(accessKey.get, secretKey.get))
      }
      else {
        new AmazonDynamoDBClient()
      }
    dynamoClient.withRegion(region)
    if (endpoint.isDefined) dynamoClient.withEndpoint(endpoint.get)

    log.error("DynamoDB client configured with:")
    log.error(s"Region: $region Endpoint: $endpoint AccessKey: $accessKey SecretKey: $secretKey")

    dynamoClient
  }
}
