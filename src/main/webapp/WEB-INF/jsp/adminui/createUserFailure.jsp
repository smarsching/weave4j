<%@ include file="header.jsp" %>

<h2>Create New User</h2>
<p>
  <span class="error">New user could not be created.</span><br>
  <c:choose>
    <c:when test="${reason == 'username'}">
      The specified username is invalid.<br>
    </c:when>
    <c:when test="${reason == 'password'}">
      The specified password is invalid. A password must consist of at least
      six characters.<br>
    </c:when>
    <c:when test="${reason == 'username_already_in_use'}">
      The specified username is already in use.<br>
    </c:when>
    <c:otherwise>
      <span style="font-weight: bold;">${throwable.class.name}:</span>
      ${throwable.message}<br>
    </c:otherwise>
  </c:choose>
  <a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>
</p>

<%@ include file="footer.jsp" %>
