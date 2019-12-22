package router.models

case class EventSourcingModel(
                             messageId: String,
                             sessionId: String,
                             messageBody: String,
                             senderId: Int,
                             recipientIds: Seq[Int],
                             fromAutoReply: Boolean,
                             eventDestinations: Seq[String]
                             )

object EventSourcingModel {
  implicit val dec: io.circe.Decoder[EventSourcingModel] = io.circe.generic.semiauto.deriveDecoder[EventSourcingModel]
  implicit val enc: io.circe.Encoder[EventSourcingModel] = io.circe.generic.semiauto.deriveEncoder[EventSourcingModel]
}
