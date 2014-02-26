import com.javaposse.hungryhippo.actors.{StopCrawling, StartCrawling, ActorRefs}
import play.api._

object Global extends GlobalSettings {
  import ActorRefs._

  override def onStart(app: Application) {
    crawlControlActor ! StartCrawling
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    crawlControlActor ! StopCrawling
    Logger.info("Application shutdown...")
  }

}