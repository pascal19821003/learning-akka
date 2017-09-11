package pong

import akka.actor.{Actor, Status}

class ScalaPongActor extends Actor {
  override def receive: Receive = {
    case "Ping" => sender() ! "Pong"
    case x:Any =>{
      sender() ! Status.Failure(new Exception(s"unknown message x: ${x}"))
    }
  }
}
