<%@ include file="header.jsp" %>

<h2>Delete Your Account</h2>
<form action="<spring:url value="${urlPrefix}/deleteUserConfirm" htmlEscape="true" />" method="post">
  <p>
    Please enter the username and password for your account.<br/>
    <strong>Attention: The account will be deleted without further notice.</strong>
  </p>
  <table>
    <tr>
      <th style="text-align: left;">User</th>
      <td><input type="text" name="username"></td>
    </tr>
    <tr>
      <th style="text-align: left;">Password</th>
      <td><input type="password" name="password"></td>
    </tr>
    <tr>
      <th></th>
      <td style="text-align: right;"><input type="submit" value="Request Account Removal"></td>
    </tr>
  </table>
</form>
<br/>
<a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>

<%@ include file="footer.jsp" %>
