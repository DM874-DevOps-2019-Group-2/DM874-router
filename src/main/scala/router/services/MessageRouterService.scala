package router.services

import com.redis.RedisClient
import router.helper.{ClassLogger, LoggedException}
import router.kafka.Entrypoint
import router.models.MessageDestination

import scala.concurrent.{ExecutionContext, Future}

class MessageRouterService(
                            redisClient: RedisClient
                          )(implicit ec: ExecutionContext) extends ClassLogger {
  def handle(key: Entrypoint.Key, data: Entrypoint.Data): Future[Entrypoint.DynamicallyRoutedData] = {
    import io.circe.parser._

    decode[router.models.EventSourcingModel](data) match {
      case Left(value) => LoggedException.getInstanceFuture(withLogger(_.error(s"Failed to decode EventSourcingModel with exception ${value}")))
      case Right(value) => {
        val topics = redisClient.lrange("topics", 0, -1).getOrElse(List.empty).flatten.toSeq

        val out = value.copy(eventDestinations = topics)

        import io.circe.syntax._

        Future.successful(Seq((out.asJson.noSpaces, topics.head)))
      }
    }
  }
}
