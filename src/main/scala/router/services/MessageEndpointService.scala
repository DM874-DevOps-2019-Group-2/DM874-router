package router.services

import com.redis.RedisClient
import router.helper.{ClassLogger, LoggedException}
import router.kafka.Entrypoint
import router.models.MessageDestination

import scala.concurrent.{ExecutionContext, Future}

class MessageEndpointService(
                            redisClient: RedisClient
                          )(implicit ec: ExecutionContext) extends ClassLogger {
  def handle(key: Entrypoint.Key, data: Entrypoint.Data): Future[Entrypoint.DynamicallyRoutedData] = {
    import io.circe.parser._

    decode[router.models.EventSourcingModel](data) match {
      case Left(value) => LoggedException.getInstanceFuture(withLogger(_.error(s"Failed to decode EventSourcingModel with exception ${value}")))
      case Right(value) => {
        val asMsgDestinations = value.recipientIds.map(id => MessageDestination(value.senderId, id, value.messageBody))

        val withServiceNames = asMsgDestinations.map(x => x -> redisClient.smembers(x.destinationId).getOrElse(Set.empty).flatten)

        import io.circe.syntax._

        val out = withServiceNames.flatMap{ case (destination, topics) =>
          val stringify = destination.asJson.noSpaces

          topics.map(topic => stringify -> topic)
        }

        Future.successful(out)
      }
    }
  }
}
