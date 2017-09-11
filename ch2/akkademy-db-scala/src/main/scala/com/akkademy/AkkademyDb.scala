package com.akkademy

import java.util.Scanner

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.event.Logging
import com.akkademy.messages._

import scala.collection.mutable.HashMap
import akka.pattern.ask
import akka.util.Timeout

import akka.util.Timeout
import scala.concurrent.duration._

class AkkademyDb extends Actor {
  val map = new HashMap[String, Object]
  val log = Logging(context.system, this)

  override def receive = {
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {} value: {}", key, value)
      map.put(key, value)
      sender() ! Status.Success
    case GetRequest(key) =>
      log.info("received GetRequest - key: {}", key)
      val response: Option[Object] = map.get(key)
      response match{
        case Some(x) => sender() ! x
        case None => sender() ! Status.Failure(new KeyNotFoundException(key))
      }
    case Heartbeat(conf) =>{
      log.info(s"conf: ${conf}")
      sender() ! Status.Success
    }
    case o => Status.Failure(new ClassNotFoundException)
  }
}

object Main extends App {
  val scanner: Scanner = new Scanner(System.in)

  val system = ActorSystem("akkademy")
  val helloActor = system.actorOf(Props[AkkademyDb], name = "akkademy-db")

  //geekagent
  private implicit val timeout = Timeout(20 seconds)
  import scala.concurrent.ExecutionContext.Implicits.global

  println("please entry geek agent address, sample address 192.168.202.119:2551")
  val geekagentAddress: String = scanner.nextLine()
 // val geekagentAddress = "192.168.202.119:2551"
  val geekAgentActor = system.actorSelection(s"akka.tcp://geekAgent@$geekagentAddress/user/geekAgentActor")


  var running = true
  while(running){
    println("please input command: 1 , update configure; 0, exit the program.")
    val command: String = scanner.nextLine()

    command match{
      case "1" =>{
        val f = geekAgentActor ? ConfUpdate("sink.k.brandwith=10m")
        f.onSuccess({
          case ConfUpdateRes(msg)=> println(s"update geek agent result: ${msg}")
          case Status.Failure(ex)=>{
            println(s"failure: ${ex.getMessage}")
            ex.printStackTrace()
          }
          case o=>println(s"update geek agent, receiver unkown message: ${o}")
        })
        f.onFailure({
          case ex:Throwable=>{
            println(s"error: ${ex.getMessage}")
            ex.printStackTrace()
          }
        })
      }
      case "0" =>{
        running = false

      }
    }
  }
  system.shutdown()




}
