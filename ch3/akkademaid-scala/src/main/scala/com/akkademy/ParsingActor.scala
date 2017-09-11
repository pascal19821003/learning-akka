package com.akkademy

import akka.actor.{Actor, ActorRef, Props, Status}
import akka.event.Logging
import com.akkademy.messages.{GetRequest, KeyNotFoundException, SetRequest}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._


class ParsingActor extends Actor{
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case ParseHtmlArticle(key, html) =>
      log.info("parsing actor call ArticleBody.")
      sender() ! ArticleBody(key, de.l3s.boilerpipe.extractors.ArticleExtractor.INSTANCE.getText(html))
    case x =>
      log.info("unknown message " + x.getClass)
      sender() ! Status.Failure(new Exception("unknown message " + x.getClass))
  }
}

class CacheActor(akkademyDbPath:String, implicit val timeout: Timeout) extends  Actor{
  import scala.concurrent.ExecutionContext.Implicits.global
  val log = Logging(context.system, this)
  val akkademyDb = context.actorSelection(akkademyDbPath)

  override def receive: Receive = {
    case GetRequest(uri) => {

      val sendRef: ActorRef = sender()

      log.info(s"receive request from ${sendRef.path}, the parameter is ${uri}")

      val f = akkademyDb ? GetRequest(uri)

      f.onComplete({
        case scala.util.Success(x:String) =>{
          log.info("调用数据库成功")
          sendRef ! x
        }
        case scala.util.Failure(e:Throwable)=>{
          log.info("调用数据库失败" + e.getMessage)
          sendRef ! Status.Failure(new Exception(s"调用数据库失败,没有key为${uri}的内容" ))
        }
      })
    }

    case SetRequest(uri, body) =>{
      val f = ( akkademyDb ? SetRequest(uri,body))
      f.onSuccess({
        case Status.Success=>{
          log.info("添加到数据库成功")
         // sender() ! Status.Success
        }
        case o:Any=>{
          log.info("添加到数据库失败")
          sender() ! Status.Failure(new Exception(s"添加到数据库失败 key:${uri}"))
        }
      })
      f.onFailure({
        case t:Throwable => sender() ! Status.Failure(t)
      })
    }
  }
}


class HttpClientActor extends  Actor{
  val log = Logging(context.system, this)
  override def receive: Receive = {
    case uri:String => {
      log.info(s"receive request from ${sender().path}, the parameter is ${uri}")
      sender() ! HttpResponse(Articles.article1)
    }
  }
}