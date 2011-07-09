<%@ include file="header.jsp" %>

<h2>Reset Password</h2>
<p>
  Please enter your username <c:if test="${captchasEnabled}"> and solve the captcha</c:if>:
</p>
<form action="<spring:url value="${urlPrefix}/resetPasswordStep1" htmlEscape="true" />" method="post">
  Username: <input type="text" name="username">
  <br>
  <c:if test="${captchasEnabled}">
    <script type="text/javascript">
      var RecaptchaOptions = {
        theme : 'white'
      };
    </script>
    <script type="text/javascript" src="https://www.google.com/recaptcha/api/challenge?k=${recaptchaPublicKey}">
    </script>
    <noscript>
      <iframe src="https://www.google.com/recaptcha/api/noscript?k=${recaptchaPublicKey}" height="300" width="500" frameborder="0"></iframe>
      <br>
      <textarea name="recaptcha_challenge_field" rows="3" cols="40"></textarea>
      <input type="hidden" name="recaptcha_response_field" value="manual_challenge">
    </noscript>
    <br>
  </c:if>
  <input type="submit" value="Request Password Reset"/>
  <br>
</form>
<br/>
<a href="<spring:url value="${urlPrefix}/" htmlEscape="true" />">Back to main page</a>

<%@ include file="footer.jsp" %>
