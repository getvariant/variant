package com.variant.server.test

import com.variant.core.error.CoreException._
import com.variant.core.error.CoreException
import com.variant.server.boot.ConfigLoader
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.test.spec.ServerlessSpec
import com.variant.server.test.spec.BaseSpec

class ConfigTest extends BaseSpec {

   "ConfigLoader" should {

      "function without overrides" in {

         System.clearProperty("variant.config.resource");
         System.clearProperty("variant.config.file");
         System.clearProperty("variant.foo");

         // No resource is not a problem
         val config1 = ConfigLoader.load("non-existent.conf", "/variant-default.conf")

         config1.entrySet().size() mustBe 2
         config1.getString("variant.foo") mustBe "foo"
         config1.hasPath("variant.bar") mustBe false
         config1.getInt("variant.seven") mustBe 7

         // Override defaults with existing resource
         val config2 = ConfigLoader.load("variant-override.conf", "/variant-default.conf")
         config2.entrySet().size() mustBe 3
         config2.getString("variant.foo") mustBe "foo"
         config2.getString("variant.bar") mustBe "bar"
         config2.getInt("variant.seven") mustBe 8

      }

      "throw internal execpetion if default does not exist" in {

         System.clearProperty("variant.config.resource");
         System.clearProperty("variant.config.file");
         System.clearProperty("variant.foo");

         // No Default is a problem
         val caughtEx = intercept[CoreException.Internal] {
            ConfigLoader.load("foo.bar", "bad.conf")
         }
         caughtEx.getMessage mustBe "Could not find default config resource [bad.conf]"

      }

      "throw user execpetion if override file does not exist" in {

         System.setProperty("variant.config.file", "non-existent");
         System.clearProperty("variant.config.resource");

         val caughtEx = intercept[CoreException.User] {
            ConfigLoader.load("variant.conf", "/variant-default.conf");
         }
         caughtEx.getMessage mustBe CONFIG_FILE_NOT_FOUND.asMessage("non-existent");
      }

      "throw user execpetion if override resurce does not exist" in {

         System.setProperty("variant.config.resource", "non-existent");
         System.clearProperty("variant.config.file");

         val caughtEx1 = intercept[CoreException.User] {
            ConfigLoader.load("bad.cond", "/variant-default.conf");
         }
         caughtEx1.getMessage mustBe CONFIG_RESOURCE_NOT_FOUND.asMessage("non-existent");

         val caughtEx2 = intercept[CoreException.User] {
            ConfigLoader.load("variant.conf", "/variant-default.conf");
         }
         caughtEx2.getMessage mustBe CONFIG_RESOURCE_NOT_FOUND.asMessage("non-existent");

      }

      "override from classpath" in {

         System.setProperty("variant.config.resource", "/variant-path-override.conf");
         System.clearProperty("variant.config.file");
         System.clearProperty("variant.foo");

         // No resource
         val config1 = ConfigLoader.load("bad.conf", "/variant-default.conf")
         config1.entrySet().size() mustBe 4
         config1.getString("variant.foo") mustBe "bar"
         config1.hasPath("variant.bar") mustBe false
         config1.getInt("variant.seven") mustBe 9
         config1.getBoolean("overridden.from.path") mustBe true
         config1.getBoolean("overridden.from.file") mustBe false

         // With resource
         val config2 = ConfigLoader.load("/variant-override.conf", "/variant-default.conf")
         println(config2)
         config2.entrySet().size() mustBe 5
         config2.getString("variant.foo") mustBe "bar"
         config2.getString("variant.bar") mustBe "bar"
         config2.getInt("variant.seven") mustBe 9
         config2.getBoolean("overridden.from.path") mustBe true
         config2.getBoolean("overridden.from.file") mustBe false
      }

      "override from filesystem" in {

         System.setProperty("variant.config.file", "src/test/resources/variant-file-override.conf")
         System.clearProperty("variant.config.resource");
         System.clearProperty("variant.foo");

         // No resource
         val config1 = ConfigLoader.load("bad.conf", "variant-default.conf")
         config1.entrySet().size() mustBe 4
         config1.getString("variant.foo") mustBe "bar"
         config1.hasPath("variant.bar") mustBe false
         config1.getInt("variant.seven") mustBe 9
         config1.getBoolean("overridden.from.path") mustBe false
         config1.getBoolean("overridden.from.file") mustBe true

         // With resource
         val config2 = ConfigLoader.load("variant-override.conf", "variant-default.conf")
         config2.entrySet().size() mustBe 5
         config2.getString("variant.foo") mustBe "bar"
         config2.getString("variant.bar") mustBe "bar"
         config2.getInt("variant.seven") mustBe 9
         config2.getBoolean("overridden.from.path") mustBe false
         config2.getBoolean("overridden.from.file") mustBe true
      }

      "override individual properties" in {

         System.clearProperty("variant.config.resource");
         System.clearProperty("variant.config.file");

         System.setProperty("xyz", "xyz");
         System.setProperty("variant.foo", "xyz");

         val config = ConfigLoader.load("variant-override.conf", "variant-default.conf");
         config.entrySet().size() mustBe 3
         config.getString("variant.foo") mustBe "xyz"
         config.getString("variant.bar") mustBe "bar"
         config.getInt("variant.seven") mustBe 8

      }
   }
}
