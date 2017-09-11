package com.akkademy

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout

import scala.util.Success

/**
  * Created by pascal on 9/8/17.
  */

object AskMain extends  App{
  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  private val system: ActorSystem = ActorSystem("system")
  private implicit val timeout = Timeout(20 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val akkademyDb = system.actorOf(Props[AkkademyDb], "akkademyDb")
  val cacheActor = system.actorOf(Props(classOf[CacheActor], akkademyDb.path.toString, timeout), "cacheActor")
  val httpClientActor = system.actorOf(Props[HttpClientActor], "httpClientActor")
  val articleParserActor =system.actorOf(Props[ParsingActor], "articleParserActor")

  val askDemoActor = system.actorOf(
    Props(classOf[AskDemoArticleParser],
      "akka://system/user/cacheActor",
      "akka://system/user/httpClientActor",
      "akka://system/user/articleParserActor",
      timeout), "askDemoArticleParserActor")

  val f = askDemoActor ? ParseArticle("http://www.google.com")

  f.onComplete({
    case scala.util.Success(x:String) =>{
      println("onComplete Success: " + x)
//      val f2 = askDemoActor ? ParseArticle("http://www.google.com")
//      f2.onComplete({
//        case scala.util.Success(x:String) =>{
//          println("222onComplete Success: " + x)
//        }
//        case scala.util.Failure(t:Throwable) =>{
//          println("22onComplete Failure: "+t.getMessage)
//        }
//      })
    }
    case scala.util.Failure(x:Throwable) =>{
      println("onComplete Failure: "+x.getMessage)
    }

  })




}