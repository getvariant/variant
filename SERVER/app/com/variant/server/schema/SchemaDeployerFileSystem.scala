package com.variant.server.schema

import java.io.File
import scala.io.Source
import scala.collection.mutable
import play.api.Logger
import com.variant.server.api.ConfigKeys._
import com.variant.core.CommonError
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantServer
import com.variant.core.UserError.Severity
import scala.collection.mutable.ArrayBuffer

/**
 * Deploy schemata from a directory on the file system.
 * Multiple schema files in directory are not yet supported,
 * user error will be thrown if number of files in
 * ServerPropertiesKey.SCHEMATA_DIR is other than 1.
 */
class SchemaDeployerFileSystem() extends AbstractSchemaDeployer {

  private val logger = Logger(this.getClass)
  private val _schemata = new ArrayBuffer[ServerSchema]
  // Default is empty
  override def schemata = _schemata.toSeq

  /**
   * Read content of schemata dir and deploy.
   */
  override def bootstrap: Unit = {

    var dirName = sys.props.get(SCHEMATA_DIR)
    if (dirName.isEmpty) {
      if (!VariantServer.instance.config.hasPath(SCHEMATA_DIR))
        throw new ServerException.User(CommonError.CONFIG_PROPERTY_NOT_SET, SCHEMATA_DIR);
      dirName = Option(VariantServer.instance.config.getString(SCHEMATA_DIR))
    }

    val dir = new File(dirName.get)
    if (!dir.exists)
      throw new ServerException.User(ServerErrorLocal.SCHEMATA_DIR_MISSING, dirName.get)
    if (!dir.isDirectory)
      throw new ServerException.User(ServerErrorLocal.SCHEMATA_DIR_NOT_DIR, dirName.get)

    val schemaFiles = dir.listFiles()

    if (schemaFiles.length == 0) {
      logger.info("No schemata detected")
    } else if (schemaFiles.length > 1)
      throw new ServerException.User(ServerErrorLocal.MULTIPLE_SCHEMATA_NOT_SUPPORTED, dirName.get)

    val schemaFile = schemaFiles.head
    
    // Parse
    val parserResp = parse(Source.fromFile(schemaFile).mkString)
   
    // If failed parsing, print errors and no schema.
    if (parserResp.hasMessages(Severity.ERROR)) {
      logger.error("Schema [%s] was not deployed due to previous parser error(s)".format(schemaFile.getName));
    }
    else {
      _schemata += deploy(parserResp)
    }    
  }

}
