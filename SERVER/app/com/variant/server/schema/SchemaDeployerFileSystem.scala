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
    
      logger.info("Mounted schemata directory [%s]".format(dir.getAbsolutePath))
    
      // Parse the files in the schemata directory: the first reference of lazy dir.
      val schemaFiles = dir.listFiles()

      if (schemaFiles.length == 0) 
         logger.info(ServerErrorLocal.EMPTY_SCHEMATA.asMessage(dir.getAbsolutePath))
    
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
         val schemaName = try { parserResp.getSchema.getName} catch {case _: NullPointerException => "?" }
         logger.warn(ServerErrorLocal.SCHEMA_FAILED.asMessage(schemaName, file.getAbsolutePath))
      }
      else {
         try {
            deploy(parserResp, file.getName)
         } catch {
            case ue: ServerException.User => 
               logger.error(ue.getMessage)
               logger.warn(ServerErrorLocal.SCHEMA_FAILED.asMessage( parserResp.getSchema.getName, file.getAbsolutePath))
         }
      }
   }

   /**
    * Undeploy a single schema from a FS file.
    */
   private def undeployFrom(file: File) = {
      logger.info("Undeploying schema from file [%s]".format(file.getAbsolutePath))
      _schemata.undeploy(file.getName)
   }

   /**
    * Directory watcher receives events form the file system and processes them asynchronously.
    */
   private class SchemataDirectoryWatcher extends AsyncDirectoryWatcher(dir.toPath()) {

      override def onCreate(file: Path): Unit = {
         val inFile = dir.toPath.resolve(file).toFile()
         logger.debug(s"Detected new file [${inFile}]")
         deployFrom(inFile)
      }
   
      override def onDelete(file: Path): Unit = {
         val inFile = dir.toPath.resolve(file).toFile()
         logger.debug(s"Detected deleted file [${inFile}]")
         undeployFrom(inFile)
      }
      
      override def onModify(file: Path): Unit = {
         val inFile = dir.toPath.resolve(file).toFile()
         logger.debug(s"Detected modified file [${inFile}]")
         deployFrom(inFile)
      }
   }
}


