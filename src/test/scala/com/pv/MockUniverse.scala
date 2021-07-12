/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv

import com.pv.interaction.IoInterface
import com.pv.tools.crypto.CryptoHelper
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import com.pv.common.manager.Handlers
import com.pv.common.manager.VaultManager
import com.pv.common.manager.userinterface.UserInterfaceImpl
import com.pv.common.vault.CredentialManager
import com.pv.common.vault.UserHandle
import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.credential.CredentialConfigs
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.common.vault.metadata_manager.SerializedMetadataTableUtil
import com.pv.common.vault.metadata_manager.UserTable
import com.pv.common.vault.metadata_manager.VaultTable
import com.pv.tools.crypto.MyCryptoHandle
import scala.collection.mutable
import scala.util.Random

object MockInterface {
  def get(): MockInterface = new MockInterface()
}

class MockInterface extends IoInterface {

  var nextStringToInput: mutable.Queue[String] = mutable.Queue.empty

  var nextStringOutputToVerify: mutable.Queue[String] = mutable.Queue.empty

  val getRandomString: String = Random.nextString(10)

  override def getString(prompt: String = ""): String = {
    if(nextStringToInput.nonEmpty) {
      nextStringToInput.dequeue()
    } else {
      getRandomString
    }
  }

  override def getPasswordInput(prompt: String = ""): String =
    getString()

  override def putString(output: String, prompt: String = ""): Unit = {
    if(nextStringOutputToVerify.nonEmpty) {
      nextStringOutputToVerify.dequeue() shouldBe output
    }
  }

  override def putPasswordString(output: String, prompt: String = ""): Unit =
    putString(output)

}

object MockMetadataManager {
  def get(user: String): MetadataManager = new MockMetadataManager()
}


class MockMetadataManager extends MetadataManager {

  var userDb: mutable.HashMap[UserTable.indexType, String] =
    new mutable.HashMap[UserTable.indexType, String]()

  var vaultDb: mutable.HashMap[VaultTable.indexType, String] =
    new mutable.HashMap[VaultTable.indexType, String]()

  val vaultFile: String = "This is test"

  def initialize(): Unit = ()

  def getEntryByIndexOpt(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType
  ): Option[table.rowType] = {
    if(table.tableName == UserTable.tableName) {
      userDb.get(id.asInstanceOf[UserTable.indexType]).map(
        entry => (id, entry))
    } else if(table.tableName == VaultTable.tableName) {
      vaultDb.get(id.asInstanceOf[VaultTable.indexType]).map(
        entry => (id, entry))
    } else {
      None
    }
  }

  def getAllEntries(
    table: SerializedMetadataTableUtil
  ): List[table.rowType] = {
    if(table.tableName == UserTable.tableName) {
      userDb.map{
        case (id, entry) => (id.asInstanceOf[table.indexType], entry)
      }.toList
    } else if(table.tableName == VaultTable.tableName) {
      vaultDb.map{
        case (id, entry) => (id.asInstanceOf[table.indexType], entry)
      }.toList
    } else {
      List.empty
    }
  }

  def setEntryByIndex(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType,
    entry: String
  ): Unit = {
    if(table.tableName == UserTable.tableName) {
      userDb.update(id.asInstanceOf[UserTable.indexType], entry)
    } else if(table.tableName == VaultTable.tableName) {
      vaultDb.update(id.asInstanceOf[VaultTable.indexType], entry)
    }
  }

}

object MockUserInterface {

  def get(interface: MockInterface): MockUserInterface =
    new MockUserInterface(interface)

}

class MockUserInterface(
  interface: MockInterface
) extends UserInterfaceImpl(interface) {

  var vaultUsername: String = ""
  var vaultPassword: String = ""

  var nextCredId: Option[Int] = None
  var nextCred: Option[(String, String, String)] = None

  def getRandomString: String = Random.nextString(20)

  def setNextCredentialToChoose(i: Int): Unit = {
    nextCredId = Some(i)
  }

  def setNextCredentialToGet(
    description: String = getRandomString,
    username: String = getRandomString,
    password: String = getRandomString,
  ): Unit = {
    nextCred = Some((description, username, password))
  }

  def setNextUserToGet(
    username: String = getRandomString,
    password: String = getRandomString,
  ): Unit = {
    vaultUsername = username
    vaultPassword = password
  }


  override def putCredential(
    myCrypto: MyCryptoHandle
  )(
    credential: Credential
  ): Unit = {}

  override def getUserSpec(
    metadataManagerHandle: String => MetadataManager,
  ): UserHandle = {
    val mockInterface = MockInterface.get()
    mockInterface.nextStringToInput.enqueue(vaultUsername)
    mockInterface.nextStringToInput.enqueue(vaultPassword)

    super.getUserSpec(metadataManagerHandle)
  }

  override def getCredential(
    myCrypto: MyCryptoHandle,
  )(
    id: Int,
    descriptionOpt: Option[String] = None,
  ): Credential = {
    val (description, username, password) = nextCred.get
    nextCred = None

    interface.nextStringToInput =
      scala.collection.mutable.Queue.empty
    interface.nextStringToInput.enqueue(description)
    interface.nextStringToInput.enqueue(username)
    interface.nextStringToInput.enqueue(password)

    val cred = super.getCredential(myCrypto)(id, descriptionOpt)

    cred.getUserEntry(myCrypto) shouldBe username
    cred.getPasswordEntry(myCrypto) shouldBe password
    cred.description shouldBe description

    cred
  }

  override def chooseCredential(
    credentials: CredentialManager
  ): Option[Credential] = {

    interface.nextStringToInput =
      scala.collection.mutable.Queue.empty
    interface.nextStringToInput.enqueue(nextCredId.get.toString)

    nextCredId = None

    super.chooseCredential(credentials)
  }

  override def viewAllCredentials(
    myCrypto: MyCryptoHandle
  )(
    credentials: CredentialConfigs
  ): Unit = {}

}


object MockHandlers {
  def initialize(
    vaultUsername: String,
    vaultPassword: String
  ): Handlers = {
    val interfaceUtil: MockUserInterface =
      MockUserInterface.get(MockInterface.get())

    interfaceUtil.vaultUsername = vaultUsername
    interfaceUtil.vaultPassword = vaultPassword

    val userHandle: UserHandle =
      interfaceUtil.getUserSpec(MockMetadataManager.get)

    val metadataManager: MetadataManager = userHandle.getMetadataManager

    val credentialConfigs: CredentialManager =
      CredentialManager.get(metadataManager, userHandle.myCryptoHandle)()

    Handlers(
      userHandle,
      interfaceUtil,
      metadataManager,
      credentialConfigs,
    )
  }
}

object MockUniverse {
  def create(
    username: String = "Srinivasan",
    vaultPassword: String = "ThisIsPassword"
  ): MockUniverse = new MockUniverse(username, vaultPassword)
}

class MockUniverse(username: String, vaultPassword: String) {

  def getVaultUsername: String = username

  def getVaultPassword: String = vaultPassword

  val passwordTransformation: String =
    CryptoHelper.definitiveTransformation(vaultPassword)

  val handlers: Handlers = MockHandlers.initialize(
    vaultUsername = username,
    vaultPassword = vaultPassword
  )

  val mockInterface: MockUserInterface =
    handlers.interface.asInstanceOf[MockUserInterface]

  val mockMetadataManager: MockMetadataManager =
    handlers.metadataManager.asInstanceOf[MockMetadataManager]

  val vaultManager: VaultManager = VaultManager.initialize(handlers)

}


