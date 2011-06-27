<%@ include file="header.jsp" %>

<h2>Existing Users</h2>
<table>
  <tr>
    <th>Username</th>
    <th>E-Mail</th>
    <th>Actions</th>
  </tr>
  <c:forEach items="${users}" var="user">
  <tr>
    <td>${user.username}</td>
    <td>${user.EMail}</td>
    <td>
      <a href="<spring:url value="${urlPrefix}/user/${user.username}/changePasswordForm" htmlEscape="true" />">Change Password</a>
      <a href="<spring:url value="${urlPrefix}/user/${user.username}/deleteForm" htmlEscape="true" />">Delete</a>
    </td>
  </tr>
  </c:forEach>
</table>

<h2>Create New User</h2>
<form action="<spring:url value="${urlPrefix}/create/user" htmlEscape="true" />" method="post">
  <p>
    Username may be ommitted if e-mail address is specified.
  </p>
  <table>
    <tr>
      <th style="text-align: left;">Username</th>
      <td><input type="text" name="username"></td>
    </tr>
    <tr>
      <th style="text-align: left;">E-Mail</th>
      <td><input type="text" name="email"></td>
    </tr>
    <tr>
      <th style="text-align: left;">Password</th>
      <td><input type="password" name="password"></td>
    </tr>
    <tr>
      <th></th>
      <td style="text-align: right;"><input type="submit" value="Create User"></td>
    </tr>
  </table>
</form>

<%@ include file="footer.jsp" %>
