package eventStreaming.domain

/**
 * Created by prayagupd
 * on 11/27/15.
 */

object Event extends AbstractEvent {
  val name: String = "EventStream"

  val OffsetIndex: String = "offsetIndex"
  val Created_At: String = "createdAt"
  val Event_Type: String = "eventType"
  val Event: String = "event"
  val from: String = "from"
  val to: String = "to"
}