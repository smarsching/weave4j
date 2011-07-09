<%@ include file="header.jsp" %>

<h2>Reset Password</h2>
<p>
  An e-mail with a password reset code has been sent to your registered e-mail
  address (if present). Please enter the password reset code in the form below
  to proceed:
</p>
<form action="<spring:url value="${urlPrefix}/resetPasswordStep2" htmlEscape="true" />" method="post">
  Password Reset Code: <input type="text" name="password_reset_code">
  <br>
  <input type="hidden" name="username" value="${username}">
  <input type="hidden" name="timestamp" value="${timestamp}">
  <input type="hidden" name="challenge" value="${challenge}">
  <input type="submit" value="Proceed"/>
  <br>
</form>
<br/>
<p>
  Please keep this browser window open until you receive the e-mail with the
  password reset code. The reset code is only valid if you enter it right here
  and will not be accepted if you come back to this page later.
</p>
<a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>

<%@ include file="footer.jsp" %>
