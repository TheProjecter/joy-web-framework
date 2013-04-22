<%--
  Copyright (c) Pengyu Yang. All rights reserved
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ attribute name='key' required='true' rtexprvalue='true' %>

<c:out value='${requestScope[key]}' />