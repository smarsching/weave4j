<%@ include file="header.jsp" %>

<h2>Change Password</h2>
<p>
  <span class="error">Password could not be changed.</span><br>
  <c:choose>
    <c:when test="${reason == 'password'}">
      The specified password is invalid. A password must consist of at least
      six characters.<br>
    </c:when>
    <c:otherwise>
      <span style="font-weight: bold;">${throwable.class.name}:</span>
      ${throwable.message}<br>
    </c:otherwise>
  </c:choose>
  <a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>
</p>

<%@ include file="footer.jsp" %>
