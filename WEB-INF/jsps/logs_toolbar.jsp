
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<table width='100%' border='1'>
  <tr>
    <form method='post' action='/ddu/joy/logs?page=1' >
      <td width='60'><input name='year' size='5' value='${year}' /></td>
      <td width='40'><input name='month' size='3' value='${month}' /></td>
      <td width='140'><input type='submit' value='${res['search']}' /></td>
    </form>
    <td align='right' >
    <table border='1' width='300'>
    <tr>
    <c:if test='${prev > 0}'>
      <td width='40'><a href='/ddu/joy/logs?page=${prev}'>${res['prev']}</a></td>
    </c:if>
    <c:forEach items='${pages}' var='page' varStatus='vs'>
    <td width='30'>
      <a href='/ddu/joy/logs?page=${page}'>${page}</a>
    </td>
    </c:forEach>
    <td>
      <c:if test='${more > 0}'><a href='/ddu/joy/logs?page=${more}'>${res['more']}</a></c:if>
      &nbsp;
    </td></tr>
    </table>
    </td>
  </tr>
</table>

