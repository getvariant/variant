package com.variant.server.test.util

/**
 * Parameterized String
 * 
 * @author Igor Urisman
 */
class ParamString (private val prototype: String) {
     
   def expand(vars: (String, Any)*): String = {
      var index = 0;
      var inBraces = false
      var result = new StringBuilder
      var variable = new StringBuilder
      var iter = prototype.iterator
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
                  val value = {
                     var actual = vars.filter(_._1 == tokens(0))
                     if (actual.length == 0) {
                        // Value was not passed - use default
                        if (tokens.length == 1) throw new RuntimeException(
                              "No expansion and no default for variable %s at index %d".format(tokens(0), index-2)) 
                        tokens(1)
                     }
                     else {
                        // Value was passed
                        actual.head._2.toString
                     }
                  }
                  result.append(value)
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
      result.toString()
   }
}

/**
 * Testing
 */
object ParamString {
   
   /**
    * 
    */
   def apply(prototype:String) = new ParamString(prototype)
   
   /**
    * 
    */
   def main(args: Array[String]): Unit = {
      val ps = new ParamString(
      """
         oh ${la} l$a}{${si:bar}
      """)
      println(ps.expand("la" -> "foo", "si" -> "puke").trim)
  }
}