package router

import org.apache.kafka.streams.KafkaStreams
import router.helper.ClassLogger
import router.kafka.Entrypoint
import router.kafka.Entrypoint.{Data, OutputTopic}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Main extends App with ClassLogger {
  val dependencyInjector = new DependencyInjector()
  import scala.concurrent.ExecutionContext.Implicits.global

  val (close, streams) = dependencyInjector.endpointEntrypoint.start{ case (k, v) =>
    val out = Try {
      dependencyInjector.messageEndpointService.handle(k, v)
    } match {
      case Failure(exception) => {
        withLogger(_.error(s"Exception caught in message router ${exception}"))
        Future.successful(Seq.empty[(Data, OutputTopic)])
      }
      case Success(value) => value.recover{ case exception =>
        withLogger(_.error(s"Root future caught with exeption ${exception}"))
        Seq.empty
      }
    }
    Await.result(out, scala.concurrent.duration.Duration.Inf)
  }

  val (close2, streams2) = dependencyInjector.routerEntrypoint.start{ case (k, v) =>
    val out = Try {
      dependencyInjector.messageRouterService.handle(k, v)
    } match {
      case Failure(exception) => {
        withLogger(_.error(s"Exception caught in message router ${exception}"))
        Future.successful(Seq.empty[(Data, OutputTopic)])
      }
      case Success(value) => value.recover{ case exception =>
        withLogger(_.error(s"Root future caught with exeption ${exception}"))
        Seq.empty
      }
    }
    Await.result(out, scala.concurrent.duration.Duration.Inf)
  }

  //Block while stream are not dying
  while (
    streams.state() != KafkaStreams.State.ERROR &&
    streams.state() != KafkaStreams.State.NOT_RUNNING &&
    streams.state() != KafkaStreams.State.PENDING_SHUTDOWN &&
    streams2.state() != KafkaStreams.State.ERROR &&
    streams2.state() != KafkaStreams.State.NOT_RUNNING &&
    streams2.state() != KafkaStreams.State.PENDING_SHUTDOWN) {
    Thread.sleep(10000)
  }

  close()
  close2()
}
