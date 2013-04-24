
<%@ taglib prefix='jf' tagdir='/WEB-INF/tags' %>

<jf:errors />

<form action='validations/date' method='post'>
  <input type='text' name='date' />
  <input type='submit' value='${res["submit"]}' />
</form>

<form action='validations/date-before' method='post'>
  <input type='text' name='date' />
  <input type='submit' value='${res["submit"]}' />
</form>

<form action='validations/date-after' method='post'>
  <input type='text' name='date' />
  <input type='submit' value='${res["submit"]}' />
</form>