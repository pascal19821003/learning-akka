package pong

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.{Future}
import scala.concurrent.duration._
import scala.util.Try
/**
  * Created by pascal on 9/4/17.
  */
object Test extends App {
  private val system: ActorSystem = ActorSystem("a")
  implicit val timeout = Timeout(5 seconds)
  private val pongActor: ActorRef = system.actorOf(Props[ScalaPongActor])

  import scala.concurrent.ExecutionContext.Implicits.global

  //mapTo 返回结果类型映射
  val future = pongActor ? "Ping"
  val eventualString: Future[String] = future.mapTo[String]
  eventualString.onSuccess({
    case s:String => println(s)
  })

  //返回错误处理
  val future2 = pongActor ? "unknown"
  future2.onSuccess({
    case a:Any => println(a)
   })
  future2.onFailure({
    case t:Throwable => t.printStackTrace()
  })

  // use mapTo
  askPong("Ping").onSuccess(
    {
      case x:String => println(s"replied with: ${x}")
    }
  )

  //future执行map操作
  val f:Future[Char] = askPong("Ping").map(x=>x.charAt(0))
  f.onSuccess({
    case x:Char=>println(s"char is ${x}" )
  })

  //Promise

//   askPong("causeError").onFailure({
//    case e:Exception =>
//      val res = Promise()
//      res.failure(new Exception("failed!"))
//
//  })

  //对future执行恢复
  val f3 : Future[String] = askPong("causeError").recover({
    case t: Exception=>"default"
  })

  f3.onComplete((t:Try[String])=>{
    t.foreach((s:String)=>{
      println(s"recover :${s}")
    })
  })

  //对future执行恢复操作，做法是再调用一次
  val f4 = askPong("causeError").recoverWith({
    case e:Throwable => askPong("Ping")
  })
  f4.onSuccess({
    case r:String => println(r)
  })

  //对future结果执行flatMap，扁平操作
  val f5 = askPong("Ping").flatMap((x:String)=>{
    println(s"receive ${x}")
   askPong(x + "haha")
  })
  f5.onFailure({
    case e:Throwable=> e.printStackTrace()
  })

  //chain
  val listofFuture: List[Future[String]] = List("Ping","Ping","Ping","Ping").map(x => askPong(x))
  val futureOfList: Future[List[String]] = Future.sequence(listofFuture)
  futureOfList.onSuccess({
    case x:List[String] => x.foreach(println)
  })

  //chain， 带恢复操作。
  val listofFuture1: List[Future[String]] = List("cc","bb","aa","Pong").map(x => askPong(x).recover({
    case t:Throwable => t.getMessage
  }))
  val futureOfList1: Future[List[String]] = Future.sequence(listofFuture1)
  futureOfList1.onSuccess({
    case x:List[String] => x.foreach(println)
  })

  Thread.sleep(1000)
  system.shutdown

  def askPong(message:String):Future[String] = (pongActor?message).mapTo[String]
}
