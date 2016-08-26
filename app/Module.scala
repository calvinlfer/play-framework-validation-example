import java.time.Clock
import javax.inject.Singleton

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import controllers.PersonController
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import services.data._
import services.data.dynamodb.{DynamoDBClient, DynamoDBClientProvider, DynamoDBPersonsRepository, RepositoryExecutionContextProvider}

import scala.concurrent.ExecutionContext


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
class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    // Use the system clock as the default implementation of Clock
    bind[Clock].toInstance(Clock.systemDefaultZone)

    bind[PersonController]

    bind[ExecutionContext]
      .annotatedWith(Names.named("Repository"))
      .toProvider[RepositoryExecutionContextProvider]
      .in[Singleton]

    bind[DynamoDBClient].toProvider[DynamoDBClientProvider].in[Singleton]

    bind[PersonsRepository].to[DynamoDBPersonsRepository].in[Singleton]
  }
}

