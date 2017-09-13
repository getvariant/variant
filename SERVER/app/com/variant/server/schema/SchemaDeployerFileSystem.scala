package com.variant.server.schema

import play.api.Logger
import com.variant.server.api.ConfigKeys._
import com.variant.core.CommonError
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantServer.server
import java.io.File
import scala.io.Source
import com.variant.core.UserError.Severity

/**
 * Deploy schemata from a directory on the file system.
 * Multiple schema files in directory are not yet supported,
 * user error will be thrown if number of files in
 * ServerPropertiesKey.SCHEMATA_DIR is other than 1.
 */
class SchemaDeployerFileSystem() extends AbstractSchemaDeployer {

  private val logger = Logger(this.getClass)

  /**
   * Read content of schemata dir and deploy.
   */
  override val schemata = {

    var dirName = sys.props.get(SCHEMATA_DIR)

    if (dirName.isEmpty) {
      if (!server.config.hasPath(SCHEMATA_DIR))
        throw new ServerException.User(CommonError.CONFIG_PROPERTY_NOT_SET, SCHEMATA_DIR);
      dirName = Option(server.config.getString(SCHEMATA_DIR))
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
      logger.error("Schema was not deployed due to previous parser error(s)")
      Seq()
    }
    else {
      Seq(deploy(parserResp))
    }    
  }

}
