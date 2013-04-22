<%--
  Copyright (c) Pengyu Yang. All rights reserved
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ attribute name='key' required='true' rtexprvalue='true' %>

<table width='100%' border='0'><tr><td>
<ul>
<c:forEach items='${requestScope[key]}' var='msg'>
  <li>${msg}</li>
</c:forEach>
</ul>
</td></tr></table>