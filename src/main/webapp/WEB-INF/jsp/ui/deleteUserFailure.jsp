<%@ include file="header.jsp" %>

<h2>Delete Your Account</h2>
<p>
  <span class="error">Account could not be deleted.</span><br>
  <c:choose>
    <c:when test="${reason == 'usernameOrPassword'}">
      The specified username or password is invalid.<br>
    </c:when>
    <c:otherwise>
      <span style="font-weight: bold;">${throwable.class.name}:</span>
      ${throwable.message}<br>
    </c:otherwise>
  </c:choose>
  <br>
  <a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>
</p>

<%@ include file="footer.jsp" %>
