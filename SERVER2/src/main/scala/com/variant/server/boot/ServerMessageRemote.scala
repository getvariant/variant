package com.variant.server.boot

import akka.util.ByteString
import com.variant.core.error.ServerError
import com.variant.server.api.ServerException
import akka.http.scaladsl.model.HttpEntity
import play.api.libs.json._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.ResponseEntity

/**
 * Wrap the core ServerError type with some server side semantics.
 * I DON'T THINK WE NEED IT: THE CODE SHOULD JUST USE ServerError from core.
 * object ServerMessageRemote {
 *
 * def apply(error: ServerError) = new ServerMessageRemote(error)
 * }
 *
 * class ServerMessageRemote(val error: ServerError) {
 *
 *
 * }
 */
