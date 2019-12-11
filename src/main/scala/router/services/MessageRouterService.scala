package router.services

import com.redis.RedisClient
import router.helper.{ClassLogger, LoggedException}
import router.kafka.Entrypoint

import scala.concurrent.{ExecutionContext, Future}

class MessageRouterService(
                            redisClient: RedisClient
                          )(implicit ec: ExecutionContext) extends ClassLogger {
  def handle(key: Entrypoint.Key, data: Entrypoint.Data): Future[Entrypoint.DynamicallyRoutedData] = {
    import io.circe.parser._

    decode[router.models.EventSourcingModel](data) match {
      case Left(value) => LoggedException.getInstanceFuture(withLogger(_.error(s"Failed to decode EventSourcingModel with exception ${value}")))
      case Right(value) => {
        val destinationIds = value.messageDestinations.map{ _.destinationId }

        val result = value.messageDestinations.map{ destination =>
          destination -> redisClient.smembers(destination.destinationId)
        }

        val withTargets = result.collect{ case (destination, Some(targets)) => destination -> targets.flatten }.filter{ case (_, targets) => targets.nonEmpty }

        import io.circe.syntax._

        val out = withTargets.flatMap{ case (destination, targets) =>
          targets.map(target => destination.asJson.noSpaces -> target)
        }

        Future.successful(out)
      }
    }
  }
}
