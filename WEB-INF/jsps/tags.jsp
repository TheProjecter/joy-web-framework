
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

Tags
<table border='1'>
  <c:forEach items='${tags}' var='t' varStatus='st'>
  <c:if test='${st.index % 4 == 0}'><tr></c:if>
  <td width='120'><a href='tag/${t['id']}'>${t['tag']}</a></td>
  <c:choose>
  <c:when test='${st.last}'>
  <c:forEach begin='0' end='${4 - st.index % 4 - 2}'>
  <td width='120'>&nbsp;</td>
  </c:forEach>
  </c:when>
  <c:when test='${st.index % 4 == 3}'></tr></c:when>
  </c:choose>
  </c:forEach>
</table>
