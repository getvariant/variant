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
import com.variant.server.util.AsyncDirectoryWatcher
import java.nio.file.Path
   
/**
 * Deploy schemata from a directory on the file system.
 * Multiple schema files in directory are not yet supported,
 * user error will be thrown if number of files in
 * ServerPropertiesKey.SCHEMATA_DIR is other than 1.
 */
class SchemaDeployerFileSystem() extends AbstractSchemaDeployer {

  private[this] val logger = Logger(this.getClass)
  
  // Don't evaluate during instantiation because this singleton class is instantiated
  // by Play during application startup and we don't want a user error to derail that.
  lazy val dir = {
     
     if (!VariantServer.instance.config.hasPath(SCHEMATA_DIR))
        throw new ServerException.User(CONFIG_PROPERTY_NOT_SET, SCHEMATA_DIR);
      
     val dirName = Option(VariantServer.instance.config.getString(SCHEMATA_DIR))

     val result = new File(dirName.get)
     if (!result.exists)
        throw new ServerException.User(ServerErrorLocal.SCHEMATA_DIR_MISSING, dirName.get)
     if (!result.isDirectory)
        throw new ServerException.User(ServerErrorLocal.SCHEMATA_DIR_NOT_DIR, dirName.get)
     result
  }
  
   /**
    * Read content of schemata dir and deploy.
    */
   override def bootstrap {
    
      // Start the directory watch service.
      val schemataDirWatcher = new SchemataDirectoryWatcher();
      schemataDirWatcher.start()
    
      logger.info("File system schema deployer bootstrapped on directory [%s]".format(dir.getAbsolutePath))
    
      // Parse the files in the schemata directory: the first reference of lazy dir.
      val schemaFiles = dir.listFiles()

      if (schemaFiles.length == 0) logger.warn("No schemata detected in " + dir.getAbsolutePath)
    
      schemaFiles.foreach { deployFrom(_) }
   }
  
   /**
    * Deploy a single schema from a FS file.
    */
   private def deployFrom(file: File) = {
      
      logger.info("Deploying schema from file [%s]".format(file.getAbsolutePath))
         
      // Parse
      val parserResp = parse(Source.fromFile(file).mkString)
               
      // If failed parsing, print errors and no schema.
      if (parserResp.hasMessages(Severity.ERROR)) {
        logger.error("Schema [%s] was not deployed due to previous parser error(s)".format(file.getAbsolutePath));
      }
      else {
         deploy(parserResp, file.getName)
      }    
   }

   /**
    * Directory watcher receives events form the file system and processes them asynchronously.
    */
   private class SchemataDirectoryWatcher extends AsyncDirectoryWatcher(dir.toPath()) {

      override def onCreate(file: Path): Unit = {
         val inFile = dir.toPath.resolve(file).toFile()
         logger.debug(s"Detected new file [${inFile}] in schemata directory")
         deployFrom(inFile)
      }
   
      override def onDelete(file: Path): Unit = {
         println("***************** File deleted: " + file)
      }
      
      override def onModify(file: Path): Unit = {
         println("***************** File modified: " + file)
      }
   }
}


