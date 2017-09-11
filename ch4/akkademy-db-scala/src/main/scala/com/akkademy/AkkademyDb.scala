package com.akkademy

import akka.actor._
import akka.event.Logging
import com.akkademy.messages.{KeyNotFoundException, GetRequest, SetRequest}
import scala.collection.mutable.HashMap

class AkkademyDb extends Actor {
  val map = new HashMap[String, Object]
  val log = Logging(context.system, this)

  override def receive = {
    case x: messages.Connected =>
      log.info("case x: messages.Connected")
      sender() ! x
    case x: List[_] =>
      log.info("case x: List[_] ")
      x.foreach{
        case SetRequest(key, value, senderRef) =>
          handleSetRequest(key, value, senderRef)
        case GetRequest(key, senderRef) =>
          handleGetRequest(key, senderRef)
      }
    case SetRequest(key, value, senderRef) =>
      log.info("case SetRequest(key, value, senderRef)")
      handleSetRequest(key, value, senderRef)
    case GetRequest(key, senderRef) =>
      log.info("case GetRequest(key, senderRef)")
      handleGetRequest(key, senderRef)
    case o =>
      log.info("case o")
      log.info("unknown message")
      sender() ! Status.Failure(new ClassNotFoundException)
  }

  def handleSetRequest(key: String, value: Object, senderRef: ActorRef): Unit = {
    log.info("received SetRequest - key: {} value: {}", key, value)
    map.put(key, value)
    senderRef ! Status.Success
  }

  def handleGetRequest(key: String, senderRef: ActorRef): Unit = {
    log.info("received GetRequest - key: {}", key)
    val response: Option[Object] = map.get(key)
    response match {
      case Some(x) => {
        senderRef ! x
      }
      case None => {
        log.error(s"no key found ${key}")
        senderRef ! Status.Failure(new KeyNotFoundException(key))

      }
    }
  }
}
