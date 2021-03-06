/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.util

object MainMenuChoice extends Enumeration {
  val ADD_CREDENTIAL: Value = Value("Add a new credential")
  val VIEW_CREDENTIAL: Value = Value("View a credential")
  val LIST_ALL_CREDENTIALS: Value = Value("List all credentials")
  val EDIT_CREDENTIAL: Value = Value("Edit a credential")
  val DELETE_CREDENTIAL: Value = Value("Delete a credential")
  val SYNC_WITH_DROPBOX: Value = Value("Sync local with dropbox")
  val QUIT: Value = Value("Quit")

  type MainMenuChoice = Value
}

object UserInterfaceChoice extends Enumeration {
  val GRAPHIC_INTERFACE = 0
  val TERMINAL_INTERFACE = 1

  type UserInterfaceChoice = Int
}

object Constants {
  final val DB_USERNAME: String = "passwordVault"

  final val DELETED_STRING: String = "Im Nuked"

  final def vaultFile(username: String): String =
    s"~/myvault/passwords_$username"
}
