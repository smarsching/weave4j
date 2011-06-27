<%@ include file="header.jsp" %>

<h2>Change Password</h2>
<p>
  Set new password for user &quot;${username}&quot;:<br>
  <form action="<spring:url value="${urlPrefix}/user/${username}/changePassword" htmlEscape="true" />" method="post">
    New Password: <input type="password" name="password">
    <input type="submit" value="Change Password"/><br>
  </form>
  <br/>
  <a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>
</p>

<%@ include file="footer.jsp" %>
