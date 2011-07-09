<%@ include file="header.jsp" %>

<h2>Manage Your Account</h2>
<ul>
  <li>
    <a href="<spring:url value="${urlPrefix}/resetPassword" htmlEscape="true" />">Reset Lost Password</a>
  </li>
  <li>
    <a href="<spring:url value="${urlPrefix}/deleteUser" htmlEscape="true" />">Delete Your Account</a>
  </li>
</ul>
<%@ include file="footer.jsp" %>
