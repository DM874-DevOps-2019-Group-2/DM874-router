package router

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import router.kafka.Entrypoint
import router.services.MessageRouterService

class DependencyInjector {
  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val config: com.typesafe.config.Config = ConfigFactory.load()

  lazy val redisClient = new RedisClient(config.getString("redis.hostname"), config.getInt("redis.port"))

  lazy val entrypoint = new Entrypoint(config)
  lazy val messageRouterService = new MessageRouterService(redisClient)
}
