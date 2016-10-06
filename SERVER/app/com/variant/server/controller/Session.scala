package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller

//@Singleton -- Is this for non-shared state controllers?
class Session @Inject() extends Controller {
 
  // def index = TODO
 
  // def create = TODO
 
  def read(id: String) = Action {
     Ok("Some New Text " + id)
  }
 
  def update(id: String) = TODO
 
  def delete(id: String) = TODO
}
