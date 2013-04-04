
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fmt' uri='http://java.sun.com/jsp/jstl/fmt' %>

<html>
  <head>
    <style>
      html {overflow-y: scroll; margin: 0px;}
      body {margin: 0px}
    </style>
  </head>

  <body>
    <table width='100%' height='100%' cellpadding='0' cellspacing='0'>
      <tr height='100%'>
        <td align='center'>
          <table width='800' height='100%' border='1' style='border:1px solid red'>
            <tr height='160'><td colspan='2'>Head</td></tr>
            <tr >
              <td width='160' valign='top' >
                <tiles:insertAttribute name="lmenu" />
              </td>
              <td valign='top' >
                <table width='100%'>
                  <tr height='40'><td><tiles:insertAttribute name='toolbar' /></td></tr>
                  <tr><td>
                      <tiles:insertAttribute name="body" />
                  </td></tr>
                </table>
              </td>
            </tr>
            <tr height='60'><td colspan='2'>Footer</td></tr>
          </table>
        </td>
      </tr>
    </table>
  </body>

</html>
