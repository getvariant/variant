package com.variant.server.schema

import java.io.File
import scala.io.Source
import scala.collection.mutable
import play.api.Logger
import com.variant.server.api.ConfigKeys._
import com.variant.core.RuntimeError._
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantServer
import com.variant.core.UserError.Severity
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import com.variant.server.util.DirectoryWatcher
import java.nio.file.Path

/**
 * Deploy schemata from a directory on the file system.
 * Multiple schema files in directory are not yet supported,
 * user error will be thrown if number of files in
 * ServerPropertiesKey.SCHEMATA_DIR is other than 1.
 */
class SchemaDeployerFileSystem() extends AbstractSchemaDeployer {

  private val logger = Logger(this.getClass)
  private val _schemata = HashMap[String, ServerSchema]()
  
  // Convert internal mutable map to an immutable one for the world
  override def schemata = _schemata.toMap  

  /**
   * Read content of schemata dir and deploy.
   */
  override def bootstrap {

    // Get hold of the schemata directory.
    var dirName = sys.props.get(SCHEMATA_DIR)  // Shouldn't this work automatically for Config?
    
    if (dirName.isEmpty) {
      if (!VariantServer.instance.config.hasPath(SCHEMATA_DIR))
        throw new ServerException.User(CONFIG_PROPERTY_NOT_SET, SCHEMATA_DIR);
      dirName = Option(VariantServer.instance.config.getString(SCHEMATA_DIR))
    }

    val dir = new File(dirName.get)
    if (!dir.exists)
      throw new ServerException.User(ServerErrorLocal.SCHEMATA_DIR_MISSING, dirName.get)
    if (!dir.isDirectory)
      throw new ServerException.User(ServerErrorLocal.SCHEMATA_DIR_NOT_DIR, dirName.get)

    // Start the directory watch service.
    
    logger.info("File system deployer bootstrapped on directory [%s]".format(dir.getAbsolutePath))
    
    // Parse the files in the schemata directory.
    val schemaFiles = dir.listFiles()

    if (schemaFiles.length == 0) logger.warn("No schemata detected in " + dirName)
    
    schemaFiles.foreach { (file) => 
      
      logger.debug("Deploying schema from file [%s]".format(file.getAbsolutePath))
      
      // Parse
      val parserResp = parse(Source.fromFile(file).mkString)
               
      // If failed parsing, print errors and no schema.
      if (parserResp.hasMessages(Severity.ERROR)) {
        logger.error("Schema [%s] was not deployed due to previous parser error(s)".format(file.getAbsolutePath));
      }
      else {
         val schema = deploy(parserResp)
        _schemata += (schema.getName -> schema)
      }    
    }
  }
}

/**
 * Schemata directory watcher
 */

class SchemataDirectoryWatcher(path: Path) extends DirectoryWatcher(path) {

   override def onCreate(file: java.nio.file.Path): Unit = ???

   override def onDelete(file: java.nio.file.Path): Unit = ???

   override def onModify(file: java.nio.file.Path): Unit = ???
}