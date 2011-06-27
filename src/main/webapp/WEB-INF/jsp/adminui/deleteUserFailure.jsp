<%@ include file="header.jsp" %>

<h2>Delete User</h2>
<p>
  <span class="error">User could not be deleted.</span><br>
  <span style="font-weight: bold;">${throwable.class.name}:</span>
  ${throwable.message}<br>
  <a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>
</p>

<%@ include file="footer.jsp" %>
