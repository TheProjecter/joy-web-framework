
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>
<%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %>

<jf:errors />

<table width='100%' border='1'>
  <tr>
    <form method='post' action='/ddu/joy/logs?page=1' >
      <jf:srcPage />
      <td width='60'><input name='year' size='5' value='${year}' /></td>
      <td width='40'><input name='month' size='3' value='${month}' /></td>
      <td width='160'>
        <input type='submit' value='${res['search']}' />
        &nbsp;
        <a href='/ddu/joy/logs?search'>${res['advanced']}</a>
      </td>
    </form>
    <td >
      <table width='100%' border='0'>
        <tr>
          <td width='80'><a href='/ddu/joy/log/0'>${res['new.log']}</a></td>
          <td width='80'><a href='/ddu/joy/tag/0'>${res['new.tag']}</a></td>
          <td><a href='/ddu/joy/tags'>${res['tags']}</a></td>
        </tr>
      </table>
    </td>
  </tr>
</table>

