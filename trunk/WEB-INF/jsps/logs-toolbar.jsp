
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<table width='100%' border='1'>
  <tr>
    <form method='post' action='/ddu/joy/logs?page=1' >
      <td width='60'><input name='year' size='5' value='${year}' /></td>
      <td width='40'><input name='month' size='3' value='${month}' /></td>
      <td width='140'>
        <input type='submit' value='${res['search']}' />
        &nbsp;
        <a href='/ddu/joy/logs?search'>${res['advanced']}</a>
      </td>
    </form>
    <td align='right' > </td>
  </tr>
</table>

