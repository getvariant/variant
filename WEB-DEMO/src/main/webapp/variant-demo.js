/**
 * Variant JavaScript API demonstration.
 * Registers a Variant custom event to be triggered by a click on the submit button.
 *
variant.boot({
   url:"<%=request.getAttribute(VariantFilter.VARIANT_REQUEST_ATTRIBUTE_NAME).",
   success: function(data, textStatus) {console.log("POST returned status '" + textStatus + "' and body '" + data + "'");},
});
   
$(document).ready(function() {   
   $(':submit').click(function() {
      new variant.Event("CLICK", $(this).html()).send();   
   });
});
*/