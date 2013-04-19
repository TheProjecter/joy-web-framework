<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<style>
  textarea {resize:none;}
</style>

<table border='1' width='100%'>
  <form method='post' action='/ddu/joy/log/${id}'>
    <tr>
      <td width='80'>${res['title']}</td>
      <td ><input name='title' value='${log['title']}' /></td>
    </tr>
    <tr>
      <td>${res['content']}</td>
      <td >
        <textarea cols='40' rows='20' name='content'>${log["content"]}</textarea>
      </td>
    </tr>
    <tr>
      <td>${res['tags']}</td>
      <td >
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
      <td colspan='2' align='center'>
        <button onclick='window.location="/ddu/joy/log/${id}"' >${res["back"]}</button>
        <input type='submit' name='ok' value='${res["ok"]}' />
      </td>
    </tr>
  </form>
</table>
