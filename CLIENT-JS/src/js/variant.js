(function() {

	// Can't do w/o jQuery
	if (!window.jQuery) throw Error("jQuery is required, but wasn't found");

	// Variant name space and default properties.
	if (typeof window.variant === "undefined") {
		window.variant = {
			url: null,
			sid: null,
			schema: null,
			success: function() {},
			error: function(qXHR) {
				throw Error(
						"Bad response from Variant server: " +
						qXHR.status + ", '" + qXHR.statusText + "', '" + qXHR.responseText + "'");
			}
	    }
	}

	// privates
	var webStorage = !(typeof(Storage) === "undefined");
	var booted = false;

	// Private Event Queue.
	// Does nothing if session storage not supported by browser.
	var eventQueue = {

		/**
		 * Push an event onto the queue.
		 * local storage can only be a strging, so we store events in a json representation of an array
		 * of events. We parse in order to push and then stringify again to save.
		 */
		push: function(event) {

			if (!webStorage) return;
			
			var queueAsArray = JSON.parse(sessionStorage.variantQueue);
			queueAsArray.push(event);
			sessionStorage.variantQueue = JSON.stringify(queueAsArray);
		},
		
		drain: function() {
			
			if (!webStorage) return;	
								
			var drainer = setInterval(
				function() {
					var queueAsArray = JSON.parse(sessionStorage.variantQueue);
					if (queueAsArray.length > 0 && !variant.url) {
						clearInterval(drainer);
						throw Error("API endpoint URL has not been set. Call variant.options() to set. " + sessionStorage.variantQueue);
					}

					var cnt = 0;
					while (queueAsArray.length > 0) {
						var event = queueAsArray.pop();
						$.ajax({
							url: variant.url + 'event',
							method: "post",
							data: JSON.stringify(event),
							contentType: "application/json",
							success: variant.success,
							error: variant.error
						});
						cnt++;
					}
					sessionStorage.variantQueue = "[]";
				},
				500
			);
		}
	}

	/**
	 * Connect to a variant schema.
	 * @arg:
	 *  {
	 *     url,      -- Schema URL
	 *     success,  -- function to call on success
	 *     error     -- function to call on error
	 *  }
	 */
	variant.connect = function(url, success) {
				
		
		if (arguments.length == 0) 
			throw Error ("variant.connect() takes one or two argument");
			    
		var urlTokens = url.split(":");
		if (urlTokens.length != 4) 
			throw Error("Invalid URL. Expected 'variant:<host>:<port><path>:<schema>'. Example 'variant:localhost:5377/variant:petclinic");
		
		variant.endpoint = "http://" + urlTokens[1] + ":" + urlTokens[2];
		
		// Attempt to connect
		$.ajax({
			url: variant.endpoint + "/connection/" + urlTokens[3],
			method: "post",
			contentType: "text/plain; charset=utf-8",
			success: function (respBody, status, resp) {
				success(new variant.Connection(resp));
			},
			error: variant.error
		});

		if (webStorage && !sessionStorage.variantQueue) sessionStorage.variantQueue = "[]";

		eventQueue.drain();
		booted = true;
	}

	/*****************************************************\
	 *                variant.Connection
	 \****************************************************/	
	variant.Connection = function(resp) {
		
		this.status = resp.getResponseHeader("X-Connection-Status");
		var bodyObject = JSON.parse(resp.responseText);
		this.id = bodyObject.id;
	}

	/**
	 * get existing session by id
	 */
	variant.Connection.prototype.getSessionById = function (sid, success) {
		
	console.log("this.id " + this.id);
		$.ajax({
			url: variant.endpoint + "/session",
			method: "get",
			headers: { "X-Connection-ID": this.id },
			contentType: "text/plain; charset=utf-8",
			data: "{\"sid\":\"" + sid + "\"}",
			success: function (respBody, status, resp) {
				new variantSession(resp);
			},
			error: variant.error
		});
		
	}
	
	/*****************************************************\
	 *                variant.Session
	 \****************************************************/	
	variant.Session = function(resp) {
		console.log("GOT SESSION '" + resp + "'");
	}
	
	/*****************************************************\
	 *                variant.Event
	 \****************************************************/
	variant.Event = function(name, value, params) {

		if (!webStorage) return;

		if (arguments.length < 2) throw Error("Contructor variant.Event(name, value, params) requires at least 2 arguments.");
	  
		this.name = name;
		this.value = value;
		this.sid = variant.sid;
		this.ts = Date.now;
		this.params = params;
	}

	/**
	 * Push the event onto queue.
	 */
	variant.Event.prototype.send = function() {
		if (!webStorage) return;
		if (!booted) throw Error("Variant.JS is not booted.  Call variant.boot() first.")
		eventQueue.push(this);
	}

})();
