

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import com.variant.core.util.StringUtils
import java.util.Random
import com.variant.core.util.TimeUtils

object Foo {
   
   val rand = new Random

   def main(args: Array[String]) {

      val map1 = newMap
      var now = System.currentTimeMillis
      val removed1 = removeFilter(map1, _._2 % 2 == 0) 
      println(TimeUtils.formatDuration(System.currentTimeMillis() - now))
      println("sizes: " + map1.size + ", " + removed1.size)
            
      val map2 = newMap
      now = System.currentTimeMillis
      val removed2 = removePartition(newMap, _._2 % 2 == 0) 
      println(TimeUtils.formatDuration(System.currentTimeMillis() - now))
      println("sizes: " + map2.size + ", " + removed2.size)

   }
   
   def newMap = {
      val map = new TrieMap[String, Integer]();
      for (i <- 1 to 1000000) {
         map += StringUtils.random64BitString(rand) -> i
      }
      map
   }
   
   def removeFilter(map: mutable.Map[String, Integer], p: ((String, Integer)) => Boolean): mutable.Map[String, Integer] = {
      val result = map.filter(p)
      map --= result.keys
      result
   }
   
   def removePartition(map: mutable.Map[String, Integer], p: ((String, Integer)) => Boolean): mutable.Map[String, Integer] = {
      val (result, newMap) = map.partition({case (_,v) => v % 5 == 0 })
      result
   }

}