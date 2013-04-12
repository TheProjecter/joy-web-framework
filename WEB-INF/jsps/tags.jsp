
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

Tags
<table width='100%' border='1'>
  <c:forEach items='${tags}' var='t' varStatus='st'>
  <c:if test='${st.index % 3 == 0}'><tr></c:if>
  <td ><a href='tag/${t['id']}'>${t['tag']}</a></td>
  <c:choose>
  <c:when test='${st.last}'>
  <c:forEach begin='0' end='${3 - st.index % 3 - 2}'>
  <td>&nbsp;</td>
  </c:forEach>
  </c:when>
  <c:when test='${st.index % 3 == 2}'></tr></c:when>
  </c:choose>
  </c:forEach>
</table>
