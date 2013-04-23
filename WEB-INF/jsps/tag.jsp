
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %>

<form method='post' action='${id}'>

  <jf:srcPage />
  <jf:errors />
  
  ${res['tag']}
  <input name='tag' value='${tag}' />
  <input type='submit' value='${res['ok']}' name='ok' />
  <c:if test='${id > 0}'>
  <input type='submit' value='${res['del']}' name='delete' />
  </c:if>
</form>