// Copyright (c) Pengyu Yang. All rights reserved

package org.joyframework;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;

public class UploadFilter implements Filter {

  public void init(final FilterConfig fconfig) {

  }

  public void doFilter(final ServletRequest req, final ServletResponse resp,
                       final FilterChain fchain) throws IOException, ServletException {
    System.out.println("filter: before");
    final HttpServletRequest hreq = (HttpServletRequest)req;
    if (ServletFileUpload.isMultipartContent(hreq)) {
      final ServletFileUpload sfu = new ServletFileUpload();
      fchain.doFilter(new MultipartRequest(hreq), resp);
    }
    else {
      fchain.doFilter(req, resp);
    }
    System.out.println("filter: after");
  }

  public void destroy() {}
}

