/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.action

import java.io.Console
import scala.io.StdIn
import sun.security.util.Password
import util.enums.UserInterfaceChoice
import util.enums.UserInterfaceChoice.UserInterfaceChoice


object IoInterface {

  def getUserInterface: IoInterface = new IoInterface()

}

class IoInterface {

  val cnsl: Console = System.console();

  val interfaceType: UserInterfaceChoice = UserInterfaceChoice.TERMINAL_INTERFACE

  def getString(prompt: String = ""): String =
    StdIn.readLine(s"${prompt}: ")

  def getPasswordInput(prompt: String = ""): String = {
    if(prompt.nonEmpty)
      println(s"${prompt}: ")
    Password.readPassword(System.in).mkString
  }

  def putString(output: String, prompt: String = ""): Unit = {
    if(prompt.nonEmpty)
      print(s"${prompt}: ")
    println(output)
  }

  def putPasswordString(output: String, prompt: String = ""): Unit = {
    if(prompt.nonEmpty)
      print(s"${prompt}: ")
    println(output)
  }

}
