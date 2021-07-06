package util.vault.metadata_manager

import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import util.enums.Constants

object DbManagerImpl {

  def getOrCreateDbManager(
    username: String,
  ): MetadataManager = {

    val db = new DbManagerImpl(
      Constants.MAC_WORKING_DIRECTORY_PATH,
      username,
    )
    db.initialize()
    db

  }

  def doesFileExist(filepath: String): Boolean =
    Files.exists(Paths.get(filepath))

}

class DbManagerImpl(
  workingDirectoryPath: String,
  username: String,
) extends MetadataManager {

  val vaultFile: String =
    s"${workingDirectoryPath}/vault_${username}.db"

  private val dbPath: String = s"jdbc:sqlite:$vaultFile"

  private def getDbConnectionOpt: Option[Connection] =
    Try {
      DriverManager.getConnection(dbPath)
    }.toOption

  private def tryCloseConnection(connection: Connection): Unit =
    Try {
      connection.close()
    }

  private def getDbConnection: Connection =
    getDbConnectionOpt match {
      case Some(connection) =>
        connection
      case None =>
        throw new IllegalAccessException("Unable to open connection")
    }

  private def isTablePresent(tableName: String): Boolean = {
    val connection = getDbConnection
    val result =
      connection
        .getMetaData
        .getTables(
          null,
          null,
          tableName,
          null)
        .next()
    tryCloseConnection(connection)
    result
  }

  private def createDbIfNeeded(): Unit =
    getDbConnectionOpt.foreach(tryCloseConnection)

  private def executeSqlCommand(sql: String): Unit = {
    val connection = getDbConnection
    Try {
      connection.createStatement()
    } match {
      case Success(statement: Statement) =>
        Try {
          statement.execute(sql)
        } match {
          case Failure(exception) =>
            statement.closeOnCompletion()
            tryCloseConnection(connection)
            System.err.println(exception)
            throw exception
          case Success(_) =>
            statement.closeOnCompletion()
            tryCloseConnection(connection)
        }
      case Success(_) =>
        tryCloseConnection(connection)
      case Failure(exception) =>
        tryCloseConnection(connection)
        System.err.println(exception)
        throw exception
    }
    tryCloseConnection(connection)
  }

  private def getResultAsList(
    table: SerializedMetadataTableUtil
  )(
    rs: ResultSet,
  ): List[table.rowType] = {
    val mutableList: ListBuffer[table.rowType] = ListBuffer.empty
    while (rs.next) {
      mutableList.append(table.transform(rs))
    }
    mutableList.toList
  }


  private def executeSqlQuery(
    table: SerializedMetadataTableUtil
  )(
    sql: String,
  ): List[table.rowType] = {
    System.out.println(sql)
    getDbConnectionOpt.map {
      connection =>
        Try {
          connection.createStatement()
        } match {
          case Success(statement) =>
            Try {
              statement.executeQuery(sql)
            } match {
              case Success(results: ResultSet) =>
                val ls = getResultAsList(table)(results)
                tryCloseConnection(connection)
                ls
              case Failure(exception) =>
                tryCloseConnection(connection)
                System.err.println(exception)
                List.empty
              case Success(_) =>
                tryCloseConnection(connection)
                List.empty
            }
          case Failure(exception) =>
            tryCloseConnection(connection)
            System.err.print(exception)
            List.empty
        }
    }.getOrElse(List.empty)
  }

  def initialize(): Unit = {
    if (!DbManagerImpl.doesFileExist(dbPath))
      createDbIfNeeded()

    if (!isTablePresent(UserTable.tableName))
      executeSqlCommand(UserTable.createTableSql)

    if (!isTablePresent(VaultTable.tableName))
      executeSqlCommand(VaultTable.createTableSql)


  }

  def getEntryByIndexOpt(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType
  ): Option[table.rowType] =
    executeSqlQuery(table)(table.selectByIndexSql(id)).headOption

  def getAllEntries(
    table: SerializedMetadataTableUtil
  ): List[table.rowType] = executeSqlQuery(table)(table.selectAllSql)

  def setEntryByIndex(
    table: SerializedMetadataTableUtil
  )(
    id: table.indexType,
    entry: String
  ): Unit = executeSqlCommand(table.insertSql(id, entry))

}