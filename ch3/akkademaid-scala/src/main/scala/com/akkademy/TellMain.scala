package com.akkademy

import akka.actor.{ActorSystem, Props}

/**
  * Created by pascal on 9/8/17.
  */
object TellMain extends App{
  import akka.util.Timeout
  import scala.concurrent.duration._
  private val system: ActorSystem = ActorSystem("system")
  private implicit val timeout = Timeout(20 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val akkademyDb = system.actorOf(Props[AkkademyDb], "akkademyDb")
  val cacheActor = system.actorOf(Props(classOf[CacheActor], akkademyDb.path.toString, timeout), "cacheActor")
  val httpClientActor = system.actorOf(Props[HttpClientActor], "httpClientActor")
  val articleParserActor =system.actorOf(Props[ParsingActor], "articleParserActor")

  val tellDemoActor =  system.actorOf(Props(classOf[TellDemoArticleParser] ,
      "akka://system/user/cacheActor",
    "akka://system/user/httpClientActor",
    "akka://system/user/articleParserActor",
    timeout), "tellDemoArticleParserActor")

  tellDemoActor ! ParseArticle("http://www.google.com")
  
}
