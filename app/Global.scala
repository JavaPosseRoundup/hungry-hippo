import akka.actor.{Props, ActorRef}
import com.javaposse.hungryhippo.actors.{StopCrawling, StartCrawling, CrawlControlActor}
import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    println("onStart")
    val crawlControlActor: ActorRef = Akka.system.actorOf(Props[CrawlControlActor])
    crawlControlActor ! StartCrawling
  }

  override def onStop(app: Application) {
    val crawlControlActor: ActorRef = Akka.system.actorOf(Props[CrawlControlActor])
    crawlControlActor ! StopCrawling
  }
}