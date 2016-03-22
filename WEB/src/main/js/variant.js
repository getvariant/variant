/* Variant 0.6.0 (c) Variant, Inc. All rights reserved. getvariant.com */

// Variant namespace
if (!variant) var variant = {};

variant.Event = function(sid, name, value) {
	
	if (arguments.length != 3) throw Error("Contructor variant.Event(sid, name, value) requires 3 arguments:");
  
	this.sid = sid;
	this.name = name;
	this.value = value;
}

var e = variant.Event("SID", "NAME", "VALUE");

$.ajax({
      url: "http://localhost:8080/event",
      method: "post",
      data: '{"sid":"SESSIONID","name":"NAME","value":"VALUE","createDate":1458536110640,"parameters:[{"param1":"PARAM1"}, {"param2":"PARAM2"}]}',
      contentType: "application/json; charset=utf-8",
      success: function(data) {
         console.log(data);
      },
      error:function(jqXHR){
         throw Error("Bad response from Variant server: " + jqXHR.status + " " + jqXHR.statusText + ": " + jqXHR.responseText);
      }
   });




