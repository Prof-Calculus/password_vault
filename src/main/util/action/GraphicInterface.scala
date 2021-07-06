package util.action

import util.enums.UserInterfaceChoice
import util.enums.UserInterfaceChoice.UserInterfaceChoice

class GraphicInterface extends IoInterface {

  override val interfaceType: UserInterfaceChoice =
    UserInterfaceChoice.GRAPHIC_INTERFACE

  override def getString(prompt: String): String = ???

  override def getPasswordInput(prompt: String): String = ???

  override def putString(output: String, prompt: String = ""): Unit = ???

  override def putPasswordString(output: String, prompt: String = ""): Unit = ???
}