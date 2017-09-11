package com.akkademy

import akka.actor.{Actor, Status}
import akka.event.Logging
import com.akkademy.messages.{ConfUpdate, ConfUpdateRes}

/**
  * Created by pascal on 9/7/17.
  */
class GeekAgentActor extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case ConfUpdate(conf) =>{
      println("receive confupdate command ")
      println("update configure")
      println("restart geek agent")
      sender() ! ConfUpdateRes("ok")
    }
    case o:Any  =>{
      Status.Failure(new Exception(s"error receive info: ${o}"))
    }
  }
}
