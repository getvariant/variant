package com.variant.core.httpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

public class HttpRequest {

	private static final int DEFAULT_CONNECT_TIMEOUT = 2000;
	private static final int DEFAULT_READ_TIMEOUT = 1000;
	
	public static class Builder {
	   
      private Builder() {}

      private Optional<String> url = Optional.empty();
	   private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
      private int readTimeout = DEFAULT_READ_TIMEOUT;
	   private HttpMethod method = HttpMethod.GET;
	   private Optional<String> body = Optional.empty();
	   
      /**
	    */
	   private HttpRequest build() { 
	      url.orElseThrow(() -> new IllegalArgumentException("URL cannot be empty"));
	      return new HttpRequest(this);
	   }
	   
	   public Builder url(String url) {
	      this.url = Optional.of(url);
	      return this;
	   }

	   public Builder connectTimeout(int timeout) {
	      connectTimeout = timeout;
	      return this;
	   }

      public Builder readTimeout(int timeout) {
         readTimeout = timeout;
         return this;
      }

      public Builder method(HttpMethod method) {
         this.method = method;
         return this;
      }

      public Builder body(String body) {
         this.body = Optional.of(body);
         return this;
      }
	}
	
	public static HttpRequest build(Consumer<Builder> block) {
      
      Builder builder = new Builder();
      block.accept(builder);
      return builder.build();
   }

	private final Builder builder;
	/**
	 * 
	 * @param url
	 */
	HttpRequest(Builder builder) {
	   this.builder = builder;
	}
	
	/**
	 * Exec this HTTP request.
	 * @return
	 * @throws IOException
	 */
	public HttpResponse exec() throws IOException {		
      HttpURLConnection conn = (HttpURLConnection) new URL(builder.url.get()).openConnection();
      conn.setConnectTimeout(builder.connectTimeout);
      conn.setReadTimeout(builder.readTimeout);
      conn.setRequestMethod(builder.method.toString());
      if(builder.body.isPresent()) {
         conn.setDoOutput(true);
         OutputStream connOutputStream = conn.getOutputStream();
         OutputStreamWriter osw = new OutputStreamWriter(connOutputStream, "UTF-8");    
         osw.write(builder.body.get());
         osw.flush();
         osw.close();
         connOutputStream.close();
      }
		conn.connect();
		return new HttpResponse(conn);
	}
	
   /**
    * Static shortcut method builds and executes a default GET request.
    */
   public static HttpResponse get(String url) throws IOException {
      return build(b -> b.url(url)).exec();
   }

   /**
    * Static shortcut method builds and executes a default POST requesty.
    */
   public static HttpResponse post(String url, String body) throws IOException {
      return build(builder -> { 
         builder.method(HttpMethod.POST);
         builder.url(url); 
         builder.body = Optional.of(body); 
      }).exec();
   }

   /**
    * Static shortcut method builds and executes a default POST request with a body.
    */
   public static HttpResponse post(String url) throws IOException {
      return build(builder -> {
         builder.method(HttpMethod.POST);
         builder.url(url);
      }).exec();
   }

   /**
    * Static shortcut method builds and executes a default DELETE request.
    */
   public static HttpResponse delete(String url, String body) throws IOException {
      return build(builder -> { 
         builder.method(HttpMethod.DELETE);
         builder.url(url); 
         builder.body = Optional.of(body); 
      }).exec();
   }

}
