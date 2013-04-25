<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %>

<style>
  textarea {resize:none;}
</style>

<form method='post' action='/ddu/joy/log/${id}'>
  <jf:srcPage />
  <table border='1' width='100%'>
    <tr>
      <td width='80'>${res['title']}</td>
      <td >
        <input id='IDTitle' name='title' value='${title}' size='60' />
      </td>
    </tr>
    <tr>
      <td>${res['content']}</td>
      <td >
        <textarea cols='62' rows='20' name='content'>${content}</textarea>
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
        <input type='button' name='back' value='${res["back"]}'
               onclick='window.location="/ddu/joy/log/${id}"'/>
        <input type='submit' name='ok' value='${res["ok"]}' />
      </td>
    </tr>
  </table>
</form>
