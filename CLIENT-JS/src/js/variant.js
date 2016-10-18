(function() {

	// Can't do w/o jQuery
	if (!window.jQuery) throw Error("jQuery is required, but wasn't found");

	// Variant name space and default properties.
	if (typeof window.variant === "undefined") {
		window.variant = {
			url: null,
			success: function() {},
			error: function(jqXHR) {
				throw Error(
						"Bad response from Variant server: " + 
						jqXHR.status + " " + jqXHR.statusText + ": " + jqXHR.responseText);
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
							url: variant.url + '/event',
							method: "post",
							data: JSON.stringify(event),
							contentType: "application/json; charset=utf-8",
							success: variant.success,
							error: variant.error
						});
						cnt++;
					}
					sessionStorage.variantQueue = "[]";
				},
				500
			);
		},
		
	}

	/**
	 * Boot up Variant.js: override default properties and start the drainer.
	 */
	variant.boot = function(props) {
		
		if (booted) throw Error("Variant.js is already booted.");
		
		if (arguments.length != 0) {
			
			variant.url = props.url || variant.url;
			if (!variant.url.endsWith('/')) variant.url += "/";
			variant.success = props.success || variant.success;
			variant.error = props.error || variant.error;
		}

		if (webStorage && !sessionStorage.variantQueue) sessionStorage.variantQueue = "[]";

		eventQueue.drain();
		booted = true;
	}

	/**
	 * Event Object can be sent to server.
	 */
	variant.Event = function(name, value, params) {

		if (!webStorage) return;

		if (arguments.length < 2) throw Error("Contructor variant.Event(name, value, params) requires at least 2 arguments.");
	  
		this.name = name;
		this.value = value;
		this.params = params;
		this.sid = (function() { 
			var i = document.cookie.indexOf("variant-ssnid");
			return i < 0 ? null : document.cookie.substring(i+14,i+30);
		})();
	}

	/**
	 * Push the event onto queue.
	 */
	variant.Event.prototype.send = function() {
		if (!webStorage) return;
		if (!booted) throw Error("Variant.js is not booted.  Call variant.boot() first.")
		eventQueue.push(this);
	}

})();