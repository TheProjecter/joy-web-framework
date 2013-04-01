<%--
   unique token tag.
--%>
<%
 final java.security.SecureRandom
   sr = new java.security.SecureRandom();
 final byte[] nm = new byte[16], val = new byte[16];

 sr.nextBytes(nm);
 sr.nextBytes(val);   

 session.setAttribute("__jf_tk_name__",
   org.apache.commons.codec.binary.Base64.encodeBase64String(nm));

 session.setAttribute("__jf_tk_value__",
   org.apache.commons.codec.binary.Base64.encodeBase64String(val));   
%>

<input type='hidden'
       name='${__jf_tk_name__}' value='${__jf_tk_value__}' />
