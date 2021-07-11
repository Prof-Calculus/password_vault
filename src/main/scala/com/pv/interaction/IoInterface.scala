/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.interaction

import scala.io.StdIn
import com.pv.util.UserInterfaceChoice
import com.pv.util.UserInterfaceChoice.UserInterfaceChoice


object IoInterface {

  def getUserInterface: IoInterface = new IoInterface()

}

class IoInterface {

  val interfaceType: UserInterfaceChoice = UserInterfaceChoice.TERMINAL_INTERFACE

  def getString(prompt: String = ""): String =
    StdIn.readLine(s"${prompt}: ")

  def getPasswordInput(prompt: String = ""): String = {
    System.console() match {
      case null => getString(prompt)
      case c => c.readPassword(prompt).mkString
    }
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
