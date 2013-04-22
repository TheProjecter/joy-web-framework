<%--
  Copyright (c) Pengyu Yang. All rights reserved

  Used in HTTP form to provide the source page to which the user
  could be forwarded (or redirected) to when validation fails.

  Usage:
  Referenced in JSP file with:
  &lt;%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %&gt;

  Add
  &lt;jf:srcPage /&gt;
  to the form for which validation is performed.
--%>

<input type='hidden' name='__jf_src_page__' value='${__jf_src_page__}' />