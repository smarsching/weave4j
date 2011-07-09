<%@ include file="header.jsp" %>

<h2>Reset Password</h2>
<p>
  <span class="error">Password reset request could not be processed.</span><br>
  <c:choose>
    <c:when test="${reason == 'username'}">
      The specified username is invalid.<br>
    </c:when>
    <c:when test="${reason == 'password'}">
      The specified password is invalid. A password must consist of at least
      six characters.<br>
    </c:when>
    <c:when test="${reason == 'passwordMismatch'}">
      The two password you entered did not match.<br>
    </c:when>
    <c:when test="${reason == 'captcha'}">
      The captcha was not solved correctly.<br>
    </c:when>
    <c:when test="${reason == 'sendMail'}">
      The e-mail with the password reset code could not be sent. Maybe no e-mail address is associated with your account?<br>
    </c:when>
    <c:when test="${reason == 'passwordResetCode'}">
      The password reset code you entered is invalid. Maybe you waited to long or the mail you took the password reset code from was sent because of a different reset password request?<br>
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
