package services.data.dynamodb

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.google.inject.{Inject, Provider}
import play.api.{Configuration, Logger}


case class DynamoDBClient(underlyingClient: AmazonDynamoDBAsyncClient, prefixedTableName: String)

class DynamoDBClientProvider @Inject()(configuration: Configuration) extends Provider[DynamoDBClient] {
  private val log = Logger("DynamoDB configuration")

  override def get(): DynamoDBClient = {
    val optEndpoint = configuration.getString("dynamodb.endpoint")
    val optAccessKey = configuration.getString("dynamodb.aws-access-key-id")
    val optSecretKey = configuration.getString("dynamodb.aws-secret-access-key")
    val optRegion = configuration.getString("dynamodb.region").map(r => Regions.fromName(r))
    val optTableName = configuration.getString("dynamodb.table-name")
    val optTableNamePrefix = configuration.getString("dynamodb.table-name-prefix")

    // Dear God, please forgive me for horrible mutation
    val dynamoClient: AmazonDynamoDBAsyncClient =
      if (optAccessKey.isDefined && optSecretKey.isDefined) {
        new AmazonDynamoDBAsyncClient(new BasicAWSCredentials(optAccessKey.get, optSecretKey.get))
      }
      else {
        new AmazonDynamoDBAsyncClient()
      }

    if (optRegion.isDefined) dynamoClient.withRegion(optRegion.get)
    if (optEndpoint.isDefined) dynamoClient.withEndpoint(optEndpoint.get)

    val optPrefixedTableName = for {
      tableNamePrefix <- optTableNamePrefix
      tableName <- optTableName
    } yield s"$tableNamePrefix-$tableName"

    log.info("DynamoDB client configured with:")
    optRegion.foreach(region => log.info(s"AWS Region: ${region.toString.toLowerCase}"))
    optEndpoint.foreach(endpoint => log.info(s"Endpoint: $endpoint"))
    optAccessKey.foreach(_ => log.info("AWS Access Key ID has been provided"))
    optSecretKey.foreach(_ => log.info("AWS Secret Access Key has been provided"))
    optTableNamePrefix.foreach(prefix => log.info(s"Table prefix has been set to $prefix"))
    optTableName.foreach(tableName => log.info(s"Table name has been set to $tableName"))

    DynamoDBClient(underlyingClient = dynamoClient, optPrefixedTableName.getOrElse("defaulted-persons"))
  }
}
