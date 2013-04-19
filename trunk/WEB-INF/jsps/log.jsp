
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<table border='1' width='100%'>
  <tr>
    <td width='30'>&nbsp;</td>
    <td >${log['title']}</td>
    <td width='40'>
      <a href='/ddu/joy/logs?page=${page}&${all}'>${res['back']}</a>
    </td>
    <td width='40'>
      <a href='/ddu/joy/log/${log['id']}?edit&page=${page}&${all}'>${res['edit']}</a>
    </td>
    <td width='50'>
      <a href='/ddu/joy/log/${log['id']}?delete&${all}'>${res['del']}</a>
    </td>
  </tr>
  <tr>
    <td colspan='5'>
      ${log['content']}
    </td>
  </tr>
  <tr>
    <td colspan='5'>
      <table>
        <c:forEach items='${log["tags"]}' var='t' varStatus='vs'>
        <c:if test='${vs.index % 4 == 0}' ><tr></c:if>
        <td width='80'>${t['tag']}</td>
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
</table>
