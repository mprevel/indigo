package indigo

import scala.scalajs.js.annotation._

trait GameLauncher {

  val DefaultContainerId: String = "indigo-container"

  protected def ready(parentElementId: String, flags: Map[String, String]): Unit

  @JSExport
  def launch(): Unit =
    ready(DefaultContainerId, Map[String, String]())

  @JSExport
  def launch(containerId: String): Unit =
    ready(containerId, Map[String, String]())

  // JS API
  @JSExport
  def launch(flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(DefaultContainerId, flags.toMap)

  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(containerId, flags.toMap)

}
