<style>
  textarea {resize:none;}
</style>

<table border='1' width='100%'>
  <form method='post' action='/ddu/joy/log'>
    <tr>
      <td width='80'>${res['title']}</td>
      <td ><input name='title' /></td>
    </tr>
    <tr>
      <td>${res['content']}</td>
      <td >
        <textarea cols='40' rows='20' name='content'></textarea>
      </td>
    </tr>
    <tr>
      <td colspan='2' align='center'>
        <input type='submit' name='Ok' value='${res["ok"]}' />
      </td>
    </tr>
  </form>
</table>
