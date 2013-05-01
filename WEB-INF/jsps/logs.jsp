
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<table width='100%' border='1'>
  <tr>
    <td colspan='4'>
      <table width='100%'><tr>
        <td align='right'>
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
              </td></tr>
          </table>
        </td>
      </tr></table>
    </td>
  </tr>
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
