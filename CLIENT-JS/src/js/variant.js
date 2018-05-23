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
	if ((typeof(Storage) === "undefined")) {
		throw Error("Webstorage required but is not supported by this browser.");
	}
	
	// Private Event Queue.
	// Does nothing if session storage not supported by browser.
	var eventQueue = {

		/**
		 * Push an event onto the queue.
		 * local storage can only be a string, so we store events in a json representation of an array
		 * of events. We parse in order to push and then stringify again to save.
		 */
		push: function(event) {
			var queueAsArray = JSON.parse(sessionStorage.variantQueue);
			queueAsArray.push(event);
			sessionStorage.variantQueue = JSON.stringify(queueAsArray);
		},
		
		drain: function() {
			
			var drainer = setInterval(
				function() {
					var queueAsArray = JSON.parse(sessionStorage.variantQueue);
					var cnt = 0;
					while (queueAsArray.length > 0) {
						var event = queueAsArray.pop();
						// The event object is just as we need it, except the ssn field has the
						// entire session object, which we need to replace with sid.
						var cid = event.ssn.conn.id;
						event.sid = event.ssn.id;
						delete(event.ssn);
						$.ajax({
							url: variant.endpoint + '/event',
							method: "post",
							data: JSON.stringify(event),
							headers: { "X-Connection-ID": cid },
							contentType: "text/plain; charset=utf-8",
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
		
		$.ajax({
			url: variant.endpoint + "/connection/" + urlTokens[3],
			method: "post",
			contentType: "text/plain; charset=utf-8",
			success: function (respBody, status, resp) {
				success(new variant.Connection(resp));
			},
			error: variant.error
		});

		if (!sessionStorage.variantQueue) sessionStorage.variantQueue = "[]";

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
		
		var conn = this;
		
		$.ajax({
			url: variant.endpoint + "/session/" + sid,
			method: "get",
			headers: { "X-Connection-ID": this.id },
			contentType: "text/plain; charset=utf-8",
			success: function (respBody, status, resp) {
				var ssn = new variant.Session(conn, resp);
				success(ssn);
			},
			error: variant.error
		});
		
	}
	
	/*****************************************************\
	 *                variant.Session
	 \****************************************************/	
	variant.Session = function(conn, resp) {
		var bodyObject = JSON.parse(resp.responseText);
		this.coreSsn = JSON.parse(bodyObject.session);
		this.conn = conn;
		this.id = this.coreSsn.sid;
	}
	
	/**
	 * Send custom event to the server.
	 */
	variant.Session.prototype.triggerEvent = function(event) {
		event.ssn = this;
		eventQueue.push(event);
	}
	
	/*****************************************************\
	 *                variant.Event
	 \****************************************************/
	variant.Event = function(name, value, params) {

		if (arguments.length < 2) throw Error("Contructor variant.Event(name, value, params) requires at least 2 arguments.");
	  
		this.name = name;
		this.value = value;
		this.ts = Date.now;
		this.params = params;
	}

})();
