package com.variant.server.schema

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._
import com.variant.share.schema.{ Schema => CoreSchema }
import com.variant.share.schema.parser.ParserResponse
import com.variant.share.util.StringUtils
import com.variant.server.boot.Runtime
import com.variant.server.util.JavaImplicits._
import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerExceptionInternal
import akka.actor.PoisonPill

/**
 *
 */
object SchemaGen {

   /**
    * Lifecycle states of a schema generation.
    */
   object State extends Enumeration {
      type State = Value
      val New, Live, Dead = Value
   }

   private val rand = new java.util.Random()

   def apply(response: ParserResponse, origin: String)(implicit server: VariantServer) = new SchemaGen(response, origin)
}

/**
 * Server side schema adds some server specific state.
 */
class SchemaGen(val response: ParserResponse, val origin: String)(implicit server: VariantServer) extends CoreSchema {

   import SchemaGen._

   private[this] val coreSchema = response.getSchema

   private[this] var _state = State.New

   val id = StringUtils.random64BitString(SchemaGen.rand)

   /**
    * Number of sessions connected to this schema generation.
    */
   val sessionCount = new AtomicInteger(0)

   /*------------------------------------ Public Implementations ------------------------------------*/

   override def getMeta = {
      coreSchema.getMeta
   }

   override def getStates = {
      coreSchema.getStates
   }

   override def getState(name: String) = {
      coreSchema.getState(name)
   }

   override def getVariations() = {
      coreSchema.getVariations
   }

   override def getVariation(name: String) = {
      coreSchema.getVariation(name)
   }

   /*------------------------------------ Public Extensions ------------------------------------*/

   def state = _state

   val runtime = new Runtime(this)

   val source = response.getSchemaSrc

   val hooksService = response.getParser.getHooksService.asInstanceOf[ServerHooksService]

   val flusherService = response.getParser.getFlusherService.asInstanceOf[ServerFlusherService]

   //val eventWriter = new TraceEventWriter(flusherService)

   def goLive() {
      if (state != State.New) throw ServerExceptionInternal(s"Inconsistent state ${state} when New expected")
      _state = State.Live

   }

   def goDead() {
      if (state != State.Live) throw ServerExceptionInternal(s"Inconsistent state ${state} when Live expected")
      _state = State.Dead
   }

   /**
    *
    */
   override def toString = s"{ServerSchema=[${getMeta.getName}], ID=[${id}]}"

}
