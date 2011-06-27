<%@ include file="header.jsp" %>

<h2>Delete User</h2>
<p>
  Do you really want to delete the user &quot;${username}&quot;?<br>
  <form action="<spring:url value="${urlPrefix}/user/${username}/delete" htmlEscape="true" />" method="post">
    <input type="submit" value="Delete User"/><br>
  </form>
  <br/>
  <a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>
</p>

<%@ include file="footer.jsp" %>
