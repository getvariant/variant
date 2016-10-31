package com.variant.server.test.util

import scala.collection.mutable.HashSet

/**
 * Parameterized String
 * 
 * @author Igor Urisman
 */
class ParamString (private val prototype: String) {
     
   def expand(bindings: (String, Any)*): String = {
      var index = 0;
      var inBraces = false
      var result = new StringBuilder
      var variable = new StringBuilder
      val boundVariables = HashSet[String]()
      val iter = prototype.iterator
      while (iter.hasNext) {
         index += 1
         var c = iter.next 
         c match {
            case '$' => {
               // If next char is {, treat as variable expansion.
               val lookAhead = iter.next()
               if (lookAhead == '{') {
                  if (inBraces) throw new RuntimeException("No closing } at index %d".format(index))
                  inBraces = true
               }
               else result.append('$').append(lookAhead)
            }
            case '}' => {
               if (inBraces) {
                  // end of variable expansion.
                  inBraces = false
                  var tokens = variable.toString().split(':')
                  val varName = tokens(0)
                  val varValue = {
                     var symbol = bindings.filter(_._1 == varName)
                     if (symbol.length == 0) {
                        // Value was not passed - use default
                        if (tokens.length == 1) throw new RuntimeException(
                              "Unbound variable %s at index %d".format(tokens(0), index-2)) 
                        tokens(1)
                     }
                     else {
                        // Value was passed
                        symbol.head._2.toString
                     }
                  }
                  result.append(varValue)
                  boundVariables += varName
                  variable.clear()
               }
               else {
                  // Not a variable expansion
                  result.append(c)
               }
            }
            case _ => {
               if (inBraces) variable.append(c)   
               else result.append(c)
            }
         }
      }
      // Ensure that all input bindings were actually used. We essentially want to catch thecase when the call
      // to expand() contains a binding (name->value) where name is mistyped and does not match any variables
      // in the body.
      bindings.foreach((b: (String,Any)) => {
         if (!boundVariables.contains(b._1)) 
            throw new RuntimeException("Unusable input binding for variable %s".format(b._1))
      })
      result.toString()
   }
}

/**
 * Testing
 */
object ParamString {
   
   /**
    */
   def apply(prototype:String) = new ParamString(prototype)
   
   /**
    * Tesging...
    */
   def main(args: Array[String]): Unit = {
      val ps = new ParamString(
      """
         oh ${la} l$a}{${si:bar}
      """)
      println(ps.expand("la" -> "foo", "si" -> "puke").trim)
  }
}