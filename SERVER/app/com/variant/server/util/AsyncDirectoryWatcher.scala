package com.variant.server.util

import java.nio.file.{Path, WatchEvent, WatchKey, Files}
import java.nio.file.StandardWatchEventKinds._
import scala.collection.JavaConversions._
import com.variant.server.api.ServerException
import play.api.Logger


/**
 * Watch a directory for create/modify/delete events.
 */
abstract class AsyncDirectoryWatcher(val path: Path) extends Thread {
  
   private val logger = Logger(this.getClass)
   
   setDaemon(true) // don't get in the way of server shutdown.
  
   setUncaughtExceptionHandler (
      new Thread.UncaughtExceptionHandler {
         override def uncaughtException(t: Thread, e: Throwable): Unit = {
          logger.error(s"Ignored uncaught ${e.getClass.getSimpleName} in directory watcher", e)
        }
      }
   )

   logger.debug(s"Starting directory watcher in [${path.toAbsolutePath()}]")
   
   val watchService = path.getFileSystem.newWatchService()
   path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

   override def run() = Iterator.continually(watchService.take()).foreach {
      key => {
         key.pollEvents() foreach {
            case event: WatchEvent[_] =>
               val target = event.context().asInstanceOf[Path]
               event.kind() match {
                  case ENTRY_CREATE => onCreate(target)
                  case ENTRY_MODIFY => onModify(target)
                  case ENTRY_DELETE => onDelete(target)
               }
         case event => throw new ServerException.Internal(s"Unknown event [${event}]")
         }
         key.reset()
      }   
   }
 
   /**
    * File create
    */
   def onCreate(file: Path): Unit
   
   /**
    * Concrete subclass will implement this
    */
   def onModify(file: Path): Unit

   /**
    * Concrete subclass will implement this
    */
   def onDelete(file: Path): Unit
   
   override def start() = {
    super.start()
    logger.debug(this.getClass.getSimpleName + " thread started for directory " + path)
  }

   /**
    * Shutdown this watcher.
    */
   override def interrupt() = {
      watchService.close()
      super.interrupt()
   }
        
}