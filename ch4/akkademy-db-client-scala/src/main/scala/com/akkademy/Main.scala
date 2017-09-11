package com.akkademy

import java.util.Scanner

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Status}
import akka.event.Logging
import akka.util.Timeout
import com.akkademy.messages.{GetRequest, KeyNotFoundException, SetRequest}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * Created by pascal on 9/11/17.
  */
object Main extends App {

//  implicit val system = ActorSystem("client-system", ConfigFactory.defaultReference())
  implicit val system = ActorSystem("client-system")
  implicit val timeout = Timeout(30 seconds)

  val extraActor: ActorRef = buildExtraActor()

  //akka.tcp://akkademy@192.168.202.142:2552

  // remoteDb = system.actorSelection(s"akka.tcp://akkademy@$remoteAddress/user/akkademy-db")
//  val remoteDb = system.actorSelection( "akka.tcp://akkademy@127.0.0.1:2552/user/db")

  val clientRef = system.actorOf(Props(classOf[HotswapClientActor], "akka.tcp://akkademy@127.0.0.1:2552/user/db"), "clientActor")

  val scanner: Scanner = new Scanner(System.in)
  var running = true
  while(running){
    println("please input command: 1 (set request), 2 (get request)  0 (exit the program).")
    val command: String = scanner.nextLine()

    command match{
      case "1" =>{
        println("please input key and value")
        val key: String = scanner.nextLine()

        println("please input key and value")
        val value: String = scanner.nextLine()

        clientRef ! SetRequest(key, value, extraActor)
      }

      case "2" =>{
        println("please input key")
        val key: String = scanner.nextLine()
        clientRef ! new GetRequest(key, extraActor)
      }
      case "0" =>{
        running = false
      }
      case o =>{
        running = false
      }
    }
  }

  system.shutdown

  private def buildExtraActor(): ActorRef = {
    println("create a anonymous actor.")
    return system.actorOf(Props(new Actor {
      override def receive = {
        case Status.Success =>
          println("extra actor, receive status.sucsess")

        case x:String =>
          println(s"extra actor, receive ${x}")

        case Status.Failure(e:KeyNotFoundException)  =>
          println(s"extra actor, receive KeyNotFoundException ${e.key}")

        case o:Any=>
          println(o)

        }
    }), "extraActor")
  }

}
