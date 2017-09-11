package com.akkademy

import akka.actor.{ActorSystem, Props}

/**
  * Created by pascal on 9/11/17.
  */

object Main extends App {
  val system = ActorSystem("akkademy")
  val helloActor = system.actorOf(Props[AkkademyDb], name = "db")
  println(helloActor.path)
}
