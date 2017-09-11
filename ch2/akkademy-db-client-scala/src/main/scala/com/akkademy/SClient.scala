package com.akkademy

import java.util.Scanner

import akka.actor.{ActorSystem, Props, Status}
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

class SClient(remoteAddress: String){
  private implicit val timeout = Timeout(20 seconds)
  private implicit val system = ActorSystem("geekAgent")

  // geek agent
  val geekAgentActor = system.actorOf(Props[GeekAgentActor], name = "geekAgentActor")

  private val remoteDb = system.actorSelection(s"akka.tcp://akkademy@$remoteAddress/user/akkademy-db")

  def set(key: String, value: Object) = {
    remoteDb ? SetRequest(key, value)
  }

  def get(key: String) = {
    remoteDb ? GetRequest(key)
  }

  def shutdown(): Unit ={
   // Thread.sleep(50000)
    system.shutdown()
  }

  def sentHeartbeat(conf:String) ={
    remoteDb ? Heartbeat(conf)
  }
}


object Main extends  App{
  import scala.concurrent.ExecutionContext.Implicits.global
  val scanner: Scanner = new Scanner(System.in)

  println("please entry geek agent address, sample address 192.168.202.142:2552")
  val logcAddress: String = scanner.nextLine()

  private val client: SClient = new SClient(logcAddress)
//  client.set("foo", "bar")
//  val f = client.get("foo")
//  f.onSuccess({
//    case x:String => println(s"the result of foo is ${x}")
//  })
//  f.onFailure({
//    case t:Throwable => t.printStackTrace()
//  })
//
//  val f2 = client.get("xx")
//  f2.onSuccess({
//    case x:String => println(x)
//  })
//  f2.onFailure({
//    case e:KeyNotFoundException=>{
//      println(s"e.key: ${e.key}")
//      e.printStackTrace()
//    }
//    case t:Throwable => {
//      println(t.getMessage)
//      t.printStackTrace()
//    }
//  })

  var running = true
  while(running){
    println("please input command: 1 , start heartbeat; 0, exit the program.")
    val command: String = scanner.nextLine()

    command match{
      case "1" =>{
        val f3  = client.sentHeartbeat("source.name=a,b")
        f3.onSuccess({
          case e:Status.Success =>{
            println("f3: success")
          }
          case Status.Failure(x:KeyNotFoundException)=>{
            println(s"f3:  x.key: ${x.key}")
          }
          case o=>println(s"f3:${o}")
        })

      }
      case "0" =>{
        running = false
      }
      case o =>{
        running = false
      }
    }
  }

  client.shutdown
}