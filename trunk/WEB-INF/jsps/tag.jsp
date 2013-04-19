
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<form method='post' action='${tag['id']}'>
  ${res['tag']}
  <input name='tag' value='${tag['tag']}' />
  <input type='submit' value='${res['ok']}' name='ok' />
  <c:if test='${tag["id"] > 0}'>
  <input type='submit' value='${res['del']}' name='delete' />
  </c:if>
</form>