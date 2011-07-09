<%@ include file="header.jsp" %>

<h2>Reset Password</h2>
<p>
  Please enter the new password you want to set for your account:
</p>
<form action="<spring:url value="${urlPrefix}/resetPasswordStep3" htmlEscape="true" />" method="post">
  <table>
    <tr>
      <th style="text-align: left;">New Password</th>
      <td><input type="password" name="password"></td>
    </tr>
    <tr>
      <th style="text-align: left;">Repeat New Password</th>
      <td><input type="password" name="password_repeat"></td>
    </tr>
    <tr>
      <th></th>
      <td style="text-align: right;"><input type="submit" value="Set New Password"></td>
    </tr>
  </table>
  <input type="hidden" name="username" value="${username}">
  <input type="hidden" name="timestamp" value="${timestamp}">
  <input type="hidden" name="challenge" value="${challenge}">
  <input type="hidden" name="password_reset_code" value="${passwordResetCode}">
</form>
<br/>
<a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>

<%@ include file="footer.jsp" %>
