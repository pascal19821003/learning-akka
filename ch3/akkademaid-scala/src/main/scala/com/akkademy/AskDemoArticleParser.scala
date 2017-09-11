package com.akkademy

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages.{GetRequest, SetRequest}

import scala.concurrent.Future

class AskDemoArticleParser(cacheActorPath: String,
                           httpClientActorPath: String,
                           acticleParserActorPath: String,
                           implicit val timeout: Timeout
                            ) extends Actor {
  val log = Logging(context.system, this)
  val cacheActor = context.actorSelection(cacheActorPath)
  println(s"cacheActor: ${cacheActor.pathString}")
  val httpClientActor = context.actorSelection(httpClientActorPath)
  println(s"httpClientActor: ${httpClientActor.pathString}")
  val articleParserActor = context.actorSelection(acticleParserActorPath)
  println(s"articleParserActor: ${articleParserActor.pathString}")
  import scala.concurrent.ExecutionContext.Implicits.global


  /**
   * Note there are 3 asks so this potentially creates 6 extra objects:
   * - 3 Promises
   * - 3 Extra actors
   * It's a bit simpler than the tell example.
   */
  override def receive: Receive = {
    case ParseArticle(uri) =>
      val senderRef = sender() //sender ref needed for use in callback (see Pipe pattern for better solution)

      val cacheResult = cacheActor ? GetRequest(uri) //ask cache actor
      cacheResult.onSuccess({
        case o:Any => log.info("cacheActor 请求成功")
      })
      val result = cacheResult.recoverWith { //if request fails, then ask the articleParseActor
        case e: Exception =>
          log.info(s"cacheActor 请求失败: e.getMessage: ${e.getMessage}")

          val fRawResult = httpClientActor ? uri

          fRawResult flatMap {
            case HttpResponse(rawArticle) =>
            {
                log.info("receive rawArticle, start to parse article")
                articleParserActor ? ParseHtmlArticle(uri, rawArticle)
            }
            case x =>{
                Future.failed(new Exception("unknown response"))
            }

          }
      }


      // take the result and pipe it back to the actor
      // (see Pipe pattern for improved implementation)
      result onComplete {
        case scala.util.Success(x: String) =>
          log.info("cached result!")
          senderRef ! x //cached result
        case scala.util.Success(x: ArticleBody) =>
          log.info(s"onComplete Success ArticleBody x ")
          cacheActor ! SetRequest(uri, x.body)
          senderRef ! x.body
        case scala.util.Failure(t) =>
          log.info(s"onComplete Failure  t : ${t}")
          senderRef ! akka.actor.Status.Failure(t)
        case x =>
          log.info("unknown message! " )
      }
  }
}
