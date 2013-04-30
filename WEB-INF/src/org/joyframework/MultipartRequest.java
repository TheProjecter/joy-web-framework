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

public class MultipartRequest extends HttpServletRequestWrapper {

  private Map<String, String[]> _params;
  private Map<String, Part[]> _parts;

  MultipartRequest(final HttpServletRequest req) {
    super(req);
    readParameters(req);
  }

  private void readParameters(final HttpServletRequest req) {
    final ServletFileUpload upload = new ServletFileUpload();
    try {
      final Parameters params = new Parameters();
      for (FileItemIterator it = upload.getItemIterator(req); it.hasNext();) {
        final FileItemStream item = it.next();
        final String name = item.getFieldName();
        if (item.isFormField()) {
          params.put(name, Streams.asString(item.openStream()));
        }
        else {
          params.put(name, new Part(item));
        }
      }
      _params = params.toParametersMap();
      _parts = params.toPartsMap();
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public Map<String, String[]> getParameterMap() {
    return Collections.unmodifiableMap(_params);
  }

  public Map<String, Part[]> getPartsMap() {
    return Collections.unmodifiableMap(_parts);
  }

  public String[] getParameterValues(final String name)  {
    final String[] values = _params.get(name);
    if (null == values) return new String[0];
    
    final String[] cp = new String[values.length];
    System.arraycopy(values, 0, cp, 0, cp.length);
    return cp;
  }

  public Enumeration<String> getParameterNames() {
    return null;
  }

  public String getParameter(final String name) {
    final String[] values = _params.get(name);
    return null == values ? null : values[0];
  }
}

class Parameters {

  private Map<String, List<String>> _params = new HashMap<String, List<String>>();
  private Map<String, List<Part>> _parts = new HashMap<String, List<Part>>();

  public Map<String, String[]> toParametersMap() {
    final Map<String, String[]> rt = new HashMap<String, String[]>();
    for (Iterator<String> it = _params.keySet().iterator(); it.hasNext();) {
      final String k = it.next();
      rt.put(k, _params.get(k).toArray(new String[0])); 
    }
    return rt;
  }

  public Map<String, Part[]> toPartsMap() {
    final Map<String, Part[]> rt = new HashMap<String, Part[]>();
    for (Iterator<String> it = _parts.keySet().iterator(); it.hasNext();) {
      final String k = it.next();
      rt.put(k, _parts.get(k).toArray(new Part[0])); 
    }
    return rt;
  }

  public void put(final String name, final String val) {
    put(_params, name, val);
  }

  public void put(final String name, final Part part) {
    put(_parts, name, part);
  }

  private void put(final Map m, final String name, final Object val) {
    List li = (List)m.get(name);
    if (null == li) {
      li = new ArrayList();
      m.put(name, li);
    }
    li.add(val);
  }
}

