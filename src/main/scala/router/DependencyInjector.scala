package router

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import router.kafka.Entrypoint
import router.services.{MessageEndpointService, MessageRouterService}

import scala.concurrent.Await
import scala.util.Try

class DependencyInjector {
  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val config: com.typesafe.config.Config = ConfigFactory.load()

  lazy val redisClient = new RedisClient(config.getString("redis.hostname"), config.getInt("redis.port"))

  lazy val endpointEntrypoint = new Entrypoint(config, "kafka.streams.topic")
  lazy val routerEntrypoint = new Entrypoint(config, "router.topic")

  lazy val messageEndpointService = new MessageEndpointService(redisClient)
  lazy val messageRouterService = new MessageRouterService(redisClient)
}
