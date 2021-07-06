import scala.util.control.Breaks.break
import util.enums.MainMenuChoice._
import util.manager.Handlers
import util.manager.VaultManager



object VaultMain{

  def run(): Unit = {
    val handlers: Handlers = Handlers.initialize()

    val vaultManager: VaultManager = VaultManager.initialize(handlers)

    while(true) {
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
        case QUIT => break
      }
    }

  }

  def main(args:Array[String]) : Unit = {
    run()
  }

}
