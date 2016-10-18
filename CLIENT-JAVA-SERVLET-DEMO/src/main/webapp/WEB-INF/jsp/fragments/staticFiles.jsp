<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!--
PetClinic :: a Spring Framework demonstration
-->

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>PetClinic :: a Spring Framework demonstration</title>


    <spring:url value="/webjars/bootstrap/2.3.0/css/bootstrap.min.css" var="bootstrapCss"/>
    <link href="${bootstrapCss}" rel="stylesheet"/>

    <spring:url value="/resources/css/petclinic.css" var="petclinicCss"/>
    <link href="${petclinicCss}" rel="stylesheet"/>

    <spring:url value="/webjars/jquery/2.0.3/jquery.js" var="jQuery"/>
    <script src="${jQuery}"></script>

	<!-- jquery-ui.js file is really big so we only load what we need instead of loading everything -->
    <spring:url value="/webjars/jquery-ui/1.10.3/ui/jquery.ui.core.js" var="jQueryUiCore"/>
    <script src="${jQueryUiCore}"></script>

	<spring:url value="/webjars/jquery-ui/1.10.3/ui/jquery.ui.datepicker.js" var="jQueryUiDatePicker"/>
    <script src="${jQueryUiDatePicker}"></script>
    
    <!-- jquery-ui.css file is not that big so we can afford to load it -->
    <spring:url value="/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css" var="jQueryUiCss"/>
    <link href="${jQueryUiCss}" rel="stylesheet"></link>
    
    <%-- Variant Demo addition start --%>
    <script src="http://getvariant.com/js/variant-0.6.3.js"></script>

    <%@ page import="com.variant.client.VariantStateRequest" %>
    <%@ page import="com.variant.client.VariantSession" %>
    <%@ page import="com.variant.client.VariantClient" %>
    <%@ page import="com.variant.client.VariantClientPropertyKeys" %>
    <%@ page import="com.variant.core.VariantProperties" %>
    <%@ page import="com.variant.client.servlet.VariantFilter" %>
    <%
        // If we're on an instrumented page VariantFilter has put the current state request in http request.
        VariantStateRequest varRequest = (VariantStateRequest)request.getAttribute(VariantFilter.VARIANT_REQUEST_ATTRIBUTE_NAME);
        if (varRequest != null) {
	        VariantSession varSession = varRequest.getSession();
   		 	VariantClient varClient = varSession.getClient();
    		VariantProperties varProps = varClient.getProperties();
    		String varSvrEndpointUrl = varProps.get(VariantClientPropertyKeys.SERVER_ENDPOINT_URL);
    %>

	    <script>
	 		variant.boot({
	   			url:"<%=varSvrEndpointUrl%>",
	   			success: function(data, textStatus) {
	   				console.log("POST returned status '" + textStatus + "' and body '" + data + "'");}
			});
	   
			$(document).ready(function() {   
	   			$(':submit').click(function() {
	      		new variant.Event("CLICK", $(this).html()).send();   
	  		 });
		});
	    </script>

	<% } %>    
    <%-- Variant Demo addition end --%>
     
</head>

