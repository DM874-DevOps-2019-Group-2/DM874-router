package router.models

object MessageDestination {
  implicit val dec: io.circe.Decoder[MessageDestination] = io.circe.generic.semiauto.deriveDecoder[MessageDestination]
}

case class MessageDestination(
                             destinationId: Int,
                             message: String,
                             fromAutoReply: Boolean
                             )
