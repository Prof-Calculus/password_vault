/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.interaction

import com.pv.util.UserInterfaceChoice
import UserInterfaceChoice.UserInterfaceChoice

class GraphicInterface extends IoInterface {

  override val interfaceType: UserInterfaceChoice =
    UserInterfaceChoice.GRAPHIC_INTERFACE

  override def getString(prompt: String): String = ???

  override def getPasswordInput(prompt: String): String = ???

  override def putString(output: String, prompt: String = ""): Unit = ???

  override def putPasswordString(output: String, prompt: String = ""): Unit = ???
}