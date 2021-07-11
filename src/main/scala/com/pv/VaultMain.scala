package com.pv

import com.pv.util.MainMenuChoice._
import com.pv.common.manager.Handlers
import com.pv.common.manager.VaultManager
import scala.util.control.Breaks

object VaultMain {

  def run(): Unit = {
    val handlers: Handlers = Handlers.initialize()

    val vaultManager: VaultManager = VaultManager.initialize(handlers)

    val loop = new Breaks
    loop.breakable {
      while (true)
        handlers.interface.printAndGetMainMenuInput() match {
          case ADD_CREDENTIAL =>
            vaultManager.addCredential()
          case VIEW_CREDENTIAL =>
            vaultManager.viewCredential()
          case LIST_ALL_CREDENTIALS =>
            vaultManager.listAllCredentials()
          case EDIT_CREDENTIAL =>
            vaultManager.editCredential()
          case DELETE_CREDENTIAL =>
            vaultManager.deleteCredential()
          case SYNC_WITH_DROPBOX =>
            vaultManager.syncWithDropbox()
          case QUIT =>
            loop.break()
        }
    }

  }

  def main(args: Array[String]): Unit = {
    run()
  }

}
