/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package util.manager

object VaultManager {

  def initialize(handlers: Handlers): VaultManager =
    VaultManagerImpl.initialize(handlers = handlers)

}

trait VaultManager{

  def addCredential()

  def viewCredential()

  def listAllCredentials()

  def editCredential()

  def deleteCredential()

  def syncWithDropbox()
}