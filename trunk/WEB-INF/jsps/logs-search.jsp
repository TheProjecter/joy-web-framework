
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %>

<form method='post' action='/ddu/joy/logs'>
  <jf:srcPage />
  <jf:errors />
  <table border='1' width='500'>
    <tr>
      <td width='80'>${res['year']}</td>
      <td width='120'><input name='year' size='5' /></td>
      <td width='80'>${res['month']}</td>
      <td ><input name='month' size='3' /></td>
    </tr>
    <tr>
      <td>${res['title']}</td>
      <td colspan='3'><input name='title' size='38' /> </td>
    </tr>
    <tr>
      <td>${res['tags']}</td>
      <td colspan='3'>
        <table>
        <c:forEach items='${tags}' var='t' varStatus='vs' >
        <c:if test='${vs.index % 4 == 0}' ><tr></c:if>
        <td width='80'><input type='checkbox' value='${t['id']}' name='tag'
                              ${t['checked'] ? 'checked' : ''}>${t['tag']}</input></td>
        <c:choose>
        <c:when test='${vs.index % 4 == 3}'></tr></c:when>
        <c:when test='${vs.last}'>
        <c:forEach begin='0' end='${4 - vs.index % 4 - 2}'>
        <td>&nbsp;</td>
        </c:forEach></tr>
        </c:when>
        </c:choose>
        </c:forEach>
        </table>
        </td>
    </tr>
    <tr>
      <td align='center' colspan='4'><input type='submit' value='${res['search']}' />
    </tr>  
  </table>
</form>