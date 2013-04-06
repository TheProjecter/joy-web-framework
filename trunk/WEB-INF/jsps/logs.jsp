
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<table width='100%' border='1'>
  <tr>
    <td width='30'>&nbsp;</td>
    <td width='30'>&nbsp;<!-- Type --></td>
    <td width='30'>ID</td>
    <td >Title</td>
  </tr>
  <c:forEach items='${logs}' var='log'>
    <tr>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
      <td>${log['id']}</td>
      <td>
        <a href='/ddu/joy/log/${log['id']}'>${log['title']}</a>
      </td>
    </tr>
  </c:forEach>
</table>
