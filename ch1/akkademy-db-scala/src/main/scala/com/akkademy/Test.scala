package com.akkademy

import akka.actor.{ActorRef, ActorSystem, Props}
import com.akkademy.messages.SetRequest

/**
  * Created by pascal on 9/4/17.
  */
object Test extends App{
  private val system: ActorSystem = ActorSystem("a")
  private val actorRef: ActorRef = system.actorOf(Props[AkkademyDb])
  actorRef ! SetRequest("key", "value1111")

  Thread.sleep(1000)

  system.shutdown()


}
