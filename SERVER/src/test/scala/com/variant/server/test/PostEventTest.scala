package com.variant.server.test

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import collection.mutable.Stack
import org.scalatest._
import com.variant.server.dispatch.Dispatcher
import net.liftweb.common.ParamFailure
import net.liftweb.common.Empty
import com.variant.server.config.UserError
import org.apache.http.HttpStatus
import com.variant.server.util.UnitSpec

/**
 * TODO: read events from database.
 */
class PostEventTest extends UnitSpec {

   "postEvent" should "parse base case" in {
      val jsonString = """
         {
            "name":"NAME",
            "value":"VALUE",
            "parameters":{
               "param1":"PARAM1",
               "param2":"PARAM2"
             }
         }"""
      
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json)
      assertResult (true, "Parse Failure") {res.isDefined}
   }
   
   it should "parse mixed case properties" in {
      val jsonString = """
      {
         "nAmE":"NAME",
         "VALUE":"VALUE",
         "parameters":{
            "pAraM1":"PARAM1",
            "PARAM2":"PARAM2"
          }
      }"""
      
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json)
      assertResult (true, "Parse Failure") {res.isDefined}
   }
   
   it should "emit ParamFailure erorr if 'name' is missing" in {
      val jsonString = """
      {
         "value":"VALUE",
         "parameters":{
            "param1":"PARAM1",
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult(true, "Expected ParamFailure but got " + res.getClass.getName) {res.isInstanceOf[ParamFailure[_]]};
      val failure = res.asInstanceOf[ParamFailure[_]];
      assertResult(UserError.errors(UserError.MissingProperty).message("name")) {failure.msg};
      assertResult(HttpStatus.SC_BAD_REQUEST) {failure.param}
   }

   it should "emit ParamFailure erorr if 'value' is missing" in {
      val jsonString = """
      {
         "name":"NAME",
         "parameters":{
            "param1":"PARAM1",
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult(true, "Expected ParamFailure but got " + res.getClass.getName) {res.isInstanceOf[ParamFailure[_]]};
      val failure = res.asInstanceOf[ParamFailure[_]];
      assertResult(UserError.errors(UserError.MissingProperty).message("value")) {failure.msg};
      assertResult(HttpStatus.SC_BAD_REQUEST) {failure.param}
   }

   it should "parse valid createDate spec" in {
      val jsonString = """
      {
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult (true, "Parse Failure") {res.isDefined}
   }

   it should "emit ParamFailure if createDate is a string" in {
      val jsonString = """
      {
         "name":"NAME",
         "value":"VALUE",
         "createDate":"1454959622350",
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult(true, "Expected ParamFailure but got " + res.getClass.getName) {res.isInstanceOf[ParamFailure[_]]};
      val failure = res.asInstanceOf[ParamFailure[_]];
      assertResult(UserError.errors(UserError.InvalidDate).message("createDate")) {failure.msg};
      assertResult(HttpStatus.SC_BAD_REQUEST) {failure.param}
   }

   it should "emit ParamFailure if createDate is an object" in {
      val jsonString = """
      {
         "name":"NAME",
         "value":"VALUE",
         "createDate":{"foo":"bar"},
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult(true, "Expected ParamFailure but got " + res.getClass.getName) {res.isInstanceOf[ParamFailure[_]]};
      val failure = res.asInstanceOf[ParamFailure[_]];
      assertResult(UserError.errors(UserError.InvalidDate).message("createDate")) {failure.msg};
      assertResult(HttpStatus.SC_BAD_REQUEST) {failure.param}
   }

   it should "emit ParamFailure if param value is not a string" in {
      val jsonString = """
      {
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":1234,
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult(true, "Expected ParamFailure but got " + res.getClass.getName) {res.isInstanceOf[ParamFailure[_]]};
      val failure = res.asInstanceOf[ParamFailure[_]];
      assertResult(UserError.errors(UserError.ParamNotAString).message("param1")) {failure.msg};
      assertResult(HttpStatus.SC_BAD_REQUEST) {failure.param}
   }

   it should "emit ParamFailure if unsupported property" in {
      val jsonString = """
      {
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "unknown":3.14,
         "parameters":{
            "param1":1234,
            "param2":"PARAM2"
          }
      }"""
      val json = parse(jsonString)
      val res = Dispatcher.postEvent(json);
      assertResult(true, "Expected ParamFailure but got " + res.getClass.getName) {res.isInstanceOf[ParamFailure[_]]};
      val failure = res.asInstanceOf[ParamFailure[_]];
      assertResult(UserError.errors(UserError.UnsupportedProperty).message("unknown")) {failure.msg};
      assertResult(HttpStatus.SC_BAD_REQUEST) {failure.param}
   }

}