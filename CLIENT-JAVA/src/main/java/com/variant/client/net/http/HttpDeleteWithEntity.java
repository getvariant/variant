package com.variant.client.net.http;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * A DELETE which accepts entities (bodies) to work around Apache's hangup
 * on not supporting bodies with DELETE.
 */
public class HttpDeleteWithEntity  extends HttpEntityEnclosingRequestBase {
	
    public final static String METHOD_NAME = "DELETE";

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
    

    public HttpDeleteWithEntity() {
        super();
    }

    public HttpDeleteWithEntity(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpDeleteWithEntity(final String uri) {
        super();
        setURI(URI.create(uri));
    }

}