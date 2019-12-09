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

        //The ids should be found distinctively
        val queryResult = destinationIds match {
          case x :: xs => Future { redisClient.mget(x.toString, xs.map(_.toString): _*) }.flatMap{
            case None => LoggedException.getInstanceFuture(withLogger(_.error(s"Redis server returned value nil from query")))
            case Some(x) => Future.successful(x)
          }
          //No ids
          case Nil => Future.successful(List.empty)
        }

        queryResult
          .map(serviceNames => serviceNames zip value.messageDestinations)
          .map(_.collect{ case (Some(serviceTopic), dest) => dest.message -> serviceTopic })
      }
    }
  }
}
