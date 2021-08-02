/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv

import com.pv.common.manager.Handlers
import com.pv.common.manager.VaultManager
import com.pv.common.manager.userinterface.UserInterfaceImpl
import com.pv.common.vault.CredentialManager
import com.pv.common.vault.UserHandle
import com.pv.common.vault.memoized_metadata.credential.Credential
import com.pv.common.vault.memoized_metadata.credential.CredentialConfigs
import com.pv.common.vault.memoized_metadata.user.UserInfoInput
import com.pv.common.vault.metadata_manager.MetadataManager
import com.pv.interaction.IoInterface
import com.pv.tools.crypto.MyCryptoHandle
import java.nio.file.Files
import org.scalatest.matchers.must.Matchers.not
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
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

object MockUserInterface {

  def get(interface: MockInterface)(
    userInfo: UserInfoInput
  ): MockUserInterface =
    new MockUserInterface(interface)(userInfo)

}

class MockUserInterface(interface: MockInterface)(
  var userInfo: UserInfoInput
) extends UserInterfaceImpl(interface) {

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


  override def putCredential(myCrypto: MyCryptoHandle)(
    credential: Credential,
    indent: String = ""
  ): Unit = {}

  override def getUserSpec(): UserInfoInput = {
    interface.nextStringToInput.enqueue(userInfo.username)
    interface.nextStringToInput.enqueue(userInfo.vaultFile)
    interface.nextStringToInput.enqueue(userInfo.dbPassword)
    interface.nextStringToInput.enqueue(userInfo.vaultPassword)

    super.getUserSpec()
  }

  override def getCredential(myCrypto: MyCryptoHandle)(
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

  override def viewAllCredentials(myCrypto: MyCryptoHandle)(
    credentials: CredentialConfigs
  ): Unit = {}

}


object MockUserInfoInput{
  def createMock(user: String, vaultFile: String): UserInfoInput =
    UserInfoInput(
      username = user,
      vaultFile = vaultFile,
      dbPassword = s"${user}dbPassword",
      vaultPassword = s"${user}vaultPassword"
    )
}


object MockHandlers {
  def initialize(userInfo: UserInfoInput): Handlers = {
    val interfaceUtil: MockUserInterface =
      MockUserInterface.get(MockInterface.get())(userInfo)

    val userInfoinput: UserInfoInput = interfaceUtil.getUserSpec()

    userInfoinput.username shouldBe userInfo.username
    userInfoinput.vaultFile shouldBe userInfo.vaultFile
    userInfoinput.dbPassword shouldBe userInfo.dbPassword
    userInfoinput.vaultPassword should not be userInfo.vaultPassword

    val metadataManager: MetadataManager =
      MockMetadataManager.get(
        MetadataManager.get(
          username = userInfo.username,
          dbPassword = userInfo.dbPassword,
          vaultFile = userInfo.vaultFile,
        )
      )

    val userHandle = UserHandle.getAndPersistUserSpec(
      username = userInfo.username,
      vaultPassword = userInfo.vaultPassword,
      metadataManager = metadataManager,
      dropboxToken = UserHandle.verifyPasswordAndGetDropboxToken(
        metadataManager = metadataManager,
        username = userInfo.username,
        vaultPassword = userInfo.vaultPassword
      )
    )

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
  def create(userInfo: UserInfoInput): MockUniverse = new MockUniverse(userInfo)

  def create(
    user: String = "Srinivasan",
    vaultFileOpt: Option[String] = None
  ): MockUniverse =
    MockUniverse.create(
      MockUserInfoInput.createMock(
        user,
        vaultFileOpt.getOrElse(
          Files.createTempFile(user, ".h2.db").toFile.getPath
        )
      )
    )
}

class MockUniverse(val userInfo: UserInfoInput) {
  val handlers: Handlers = MockHandlers.initialize(userInfo)

  val mockInterface: MockUserInterface =
    handlers.interface.asInstanceOf[MockUserInterface]

  val vaultManager: VaultManager = VaultManager.initialize(handlers)

}


