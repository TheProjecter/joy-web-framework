<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %>

<jf:errors />

<table>
  <tr><td>
    <form action='/ddu/joy/validations/date' method='post'>
      <input type='text' name='date' value='${date}' />
      <input type='submit' value='${res["submit"]}' />
    </form>
    <c:if test='${validDate}'> You provided a valid date: ${date} </c:if>
  </td></tr>
  <tr ><td style='height: 20px'> </td></tr>
  <tr><td>
    <form action='/ddu/joy/validations/email' method='post'>
      <input type='text' name='email' value='${email}' />
      <input type='submit' value='${res["submit"]}' />
    </form>
    <c:if test='${validEmail}'> You provided a valid email: ${email} </c:if>
  </td></tr>
  <tr ><td style='height: 20px'> </td></tr>
  <tr><td>
    <form action='validations/date-after' method='post'>
      <input type='text' name='date' />
      <input type='submit' value='${res["submit"]}' />
    </form>
  </td></tr>
</table>