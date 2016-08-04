<!DOCTYPE html> 

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>


<html lang="en">

<jsp:include page="../fragments/staticFiles.jsp"/>
<!-- ----------------- Variant remote events ------------------------- -->
<script>

   variant.boot({
      url:"http://localhost:8080",
      success: function(data, textStatus) {console.log("POST returned status '" + textStatus + "' and body '" + data + "'");},
      error: function(jqXHR) {
         throw Error("Bad response from Variant server: " + jqXHR.status + " " + jqXHR.statusText + ": " + jqXHR.responseText);
      }
   });
   
   $(document).ready(function() {
   
      $(':submit').click(function() {
         new variant.Event($(this).html(), "CLICK").send();   
      });
         
   });

</script>
<!-- --------------------------------------------------------------- -->

<body>
<div class="container">
    <jsp:include page="../fragments/bodyHeader.jsp"/>
    <c:choose>
        <c:when test="${owner['new']}"><c:set var="method" value="post"/></c:when>
        <c:otherwise><c:set var="method" value="put"/></c:otherwise>
    </c:choose>

    <h2>
        <c:if test="${owner['new']}">New </c:if> Owner
    </h2>
    <form:form modelAttribute="owner" method="${method}" class="form-horizontal" id="add-owner-form">
        <petclinic:inputField label="First Name" name="firstName"/>
        <petclinic:inputField label="Last Name" name="lastName"/>
        <petclinic:inputField label="Address" name="address"/>
        <petclinic:inputField label="City" name="city"/>
        <petclinic:inputField label="Telephone" name="telephone"/>

        <input type="checkbox" name="box1" > I would like to be on the email list <br>
        <input type="checkbox" name="box2" > I have read the <a href="">Terms Of Service</a> <br>

        <div class="form-actions">
            <c:choose>
                <c:when test="${owner['new']}">
                    <button type="submit">Add Owner</button>
                </c:when>
                <c:otherwise>
                    <button type="submit">Update Owner</button>
                </c:otherwise>
            </c:choose>
        </div>
    </form:form>
</div>
<jsp:include page="../fragments/footer.jsp"/>
</body>

</html>
