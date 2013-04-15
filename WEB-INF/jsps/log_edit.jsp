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
        
      </td>
    </tr>
    <tr>
      <td colspan='2' align='center'>
        <input type='submit' name='ok' value='${res["ok"]}' />
      </td>
    </tr>
  </form>
</table>
