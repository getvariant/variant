package com.variant.server.schema

import java.io.File
import java.nio.file.Path

import scala.io.Source

import com.typesafe.scalalogging.LazyLogging

import com.variant.core.error.UserError.Severity
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.boot.ServerExceptionLocal
import com.variant.server.boot.VariantServer
import com.variant.server.impl.ConfigKeys.SCHEMATA_DIR;
import com.variant.server.util.AsyncDirectoryWatcher

/**
 * Deploy schemata from a directory on the file system.
 * Multiple schema files in directory are not yet supported,
 * user error will be thrown if number of files in
 * ServerPropertiesKey.SCHEMATA_DIR is other than 1.
 */
class SchemaDeployerFileSystem() extends AbstractSchemaDeployer with LazyLogging {

  // Don't evaluate during instantiation because this singleton class is instantiated
  // by Play during application startup and we don't want a user error to derail that.
  lazy val dir = {
           
     val dirName = VariantServer.config.getSchemataDir
     val result = new File(dirName)
     if (!result.exists)
        throw new ServerExceptionLocal(ServerMessageLocal.SCHEMATA_DIR_MISSING, dirName)
     if (!result.isDirectory)
        throw new ServerExceptionLocal(ServerMessageLocal.SCHEMATA_DIR_NOT_DIR, dirName)
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
         logger.info(ServerMessageLocal.EMPTY_SCHEMATA.asMessage(dir.getAbsolutePath))
    
      schemaFiles.foreach { deployFrom(_) }
   }
  
   /**
    * Deploy a single schema from a FS file.
    */
   private def deployFrom(file: File) = {
      
      // Ignore special files.
      if (file.getName.startsWith(".")) {
          logger.debug(s"Ignoring special file ${file.getName}")  
      } 
      else {
         logger.info(ServerMessageLocal.SCHEMA_DEPLOYING.asMessage(file.getAbsolutePath))
            
         // Parse
         val parserResp = parse(Source.fromFile(file).mkString)
                  
         // If failed parsing, print errors and no schema.
         if (parserResp.hasMessages(Severity.ERROR)) {
            
            val schemaName = if (parserResp.getSchemaName == null) "?" else parserResp.getSchemaName 
            logger.warn(ServerMessageLocal.SCHEMA_FAILED.asMessage(schemaName, file.getAbsolutePath))
         }
         else {
            try {
               deploy(parserResp, file.getName)
            } catch {
               case ue: ServerExceptionLocal => 
                  logger.error(ue.getMessage)
                  logger.warn(ServerMessageLocal.SCHEMA_FAILED.asMessage( parserResp.getSchema.getMeta.getName, file.getAbsolutePath))
            }
         }
      }
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
         schemata.undeploy(inFile.getName)
      }
      
      override def onModify(file: Path): Unit = {
         val inFile = dir.toPath.resolve(file).toFile()
         logger.debug(s"Detected modified file [${inFile}]")
         deployFrom(inFile)
      }
   }
}


