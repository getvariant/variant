package com.variant.server.test;
/*
import org.junit.Before;
import org.junit.BeforeClass;




//import play.api.Configuration;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

import com.variant.core.test.VariantBaseTest;
import com.variant.server.boot.VariantServer;
import com.variant.server.event.EventWriter;

public class ServerBaseTest extends VariantBaseTest {

	   private static boolean sqlSchemaCreated = false;

	   protected static String context;
	   protected static VariantServer server;
	   
	   // Set system property, if needed
	   //sys.props += (("variant.schema.dir", "/schemas"))

	   // Override app if you need a Application with other than
	   // default parameters. 
	   // TODO: find a way to externalize it
	   @BeforeClass
	   public static void beforeTestCase() {
		   Application app = new GuiceApplicationBuilder()
/*	      .configure(
	            Map(
	                  "play.http.context" -> "/variant-test"
	                  ,"variant.schemas.dir" -> "test-schemas"
	                  ,"variant.session.timeout.secs" -> 1
	                  ,"variant.session.store.vacuum.interval.secs" -> 1
	                  ,"variant.event.writer.flush.max.delay.millis" -> 1000
	                  ,"variant.event.flusher.class.name" -> "com.variant.server.event.EventFlusherH2"
	                  ,"variant.event.flusher.class.init" ->"""{"url":"jdbc:h2:mem:variant;MVCC=true;DB_CLOSE_DELAY=-1;","user":"variant","password":"variant"}"""
	            )) *//*
	       .build();
		   
		   context = app.configuration().getString("play.http.context").get();
//				   protected val store = app.injector.instanceOf[SessionStore]
		   server = app.injector().instanceOf(VariantServer.class);

	   }
		 
	   /**
	    * Each case runs in its own JVM. Each test runs in its
	    * own instance of the test case. We want the jdbc schema
	    * created only once per jvm, but the api be instance scoped.
	    * 
	    * @throws Exception
	    * Needed by the JUnit EventWriter test which is currently off.
	    *
	   @Before
	   synchronized public void  beforeAll() {
		   if (!sqlSchemaCreated) {
			   recreateSchema(server.eventWriter);
			   sqlSchemaCreated = true;
		   }
	   }
   
	   /**
	    * @throws Exception 
	    * 
	    *
	private void recreateSchema(EventWriter ew) {
		
		val jdbc = new JdbcService(ew);
		switch (jdbc.getVendor()) {
			case JdbcService.Vendor.POSTGRES: jdbc.recreateSchema();;
			case JdbcService.Vendor.H2: jdbc.createSchema();  // Fresh in-memory DB.
		}
	}
}
*/