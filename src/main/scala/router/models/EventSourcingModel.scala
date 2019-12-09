package router.models

case class EventSourcingModel(
                             messageId: String,
                             sessionId: String,
                             senderId: Int,
                             messageDestinations: Seq[MessageDestination],
                             eventDestinations: Map[String, String]
                             )

object EventSourcingModel {
  implicit val dec: io.circe.Decoder[EventSourcingModel] = io.circe.generic.semiauto.deriveDecoder[EventSourcingModel]
}
