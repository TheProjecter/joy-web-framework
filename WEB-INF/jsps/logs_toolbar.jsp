
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<table>
  <tr>
    <form method='post' action='/ddu/joy/logs?page=1' >
      <td width='60'><input name='year' size='5' value='${year}' /></td>
      <td width='40'><input name='month' size='3' value='${month}' /></td>
      <td width='80'><input type='submit' value='${res['search']}' /></td>
    </form>
    <td align='right' width='400'>
    <table border='1' width='360'>
    <tr>
    <c:forEach begin='1' end='${pages}' var='page' varStatus='vs'>
    <td width='30'>
      <a href='/ddu/joy/logs?page=${page}'>${page}</a>
    </td>
    </c:forEach>
    <td>&nbsp;</td></tr>
    </table>
    </td>
  </tr>
</table>

