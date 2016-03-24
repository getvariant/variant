/* Variant 0.6.0 (c) Variant, Inc. All rights reserved. getvariant.com */

// Can't do w/o jQuery or local storage
if (!window.jQuery) throw Error("jQuery is required by Variant but wasn't found");

if(typeof(Storage) === "undefined")
	
// Variant name space
if (!variant) var variant = {};
variant.url = variant.url || "unset";
variant.success = function() {};
variant.error = function(jqXHR) {
	throw Error("Bad response from Variant server: " + jqXHR.status + " " + jqXHR.statusText + ": " + jqXHR.responseText);
}
	
/**
 * Returns global options currently in effect. If supplied as an argument,
 * also sets them before returning.
 */
variant.options = function(options) {
	
	if (arguments.length != 0) {	
		if (!params.endpointUrl) throw Error("Property 'endpointUrl' must be set") = endpoint;
	}
	
	return {"url": variant.url};	
}

/**
 * Event Queue
 */
variant.eventQueue = {

	push: function(event) {
		if (!sessionStorage.variantQueue) sessionStorage.variantQueue = [];
		sessionStorage.variantQueue.push(event);
	}
	
	drain(): function() {
		while (sessionStorage.variantQueue.length > 0) {
			var event = sessionStorage.variantQueue.pop();
			$.ajax({
				url: variant.url,
				method: "post",
				data: JSON.stringify(event),
				contentType: "application/json; charset=utf-8",
				success: variant.success,
				error: vriant.error
			});			
		}
	}
}

/**
 * Event Object can be sent to server.
 */
variant.Event = function(sid, name, value, params) {
	
	if (arguments.length < 3) throw Error("Contructor variant.Event(sid, name, value, params) requires at least 3 arguments");
  
	this.sid = sid;
	this.name = name;
	this.value = value;
	this.params = params;

}

///--------------------------------------------------------------------------///

var opts = variant.options({
	url:"http://localhost:8080/event",
	success: function(data) {console.log(data);}
});

console.log(opts);

