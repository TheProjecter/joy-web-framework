// Copyright (c) Pengyu Yang. All rights reserved

package org.joyframework;

import clojure.lang.IFn;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.*;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Keyword;
import clojure.lang.Var;
import clojure.lang.Namespace;

import static clojure.lang.RT.var;

import java.util.Set;
import java.util.Map;

public class JoyServlet extends HttpServlet {

  private final static String CONTENT_TYPE = "text/html; charset=windows-1252";

  private final static IFn _require = var("clojure.core", "require");
  private final static IFn _find_ns = var("clojure.core", "find-ns");
  private final static IFn _ns_pubs = var("clojure.core", "ns-publics");


  private final static String CLAY_ROUTE = "org.joyframework.route";
  private final static String RESOURCES = "org.joyframework.resources";

  private static boolean _reload_allowed = false;

  private Map _bootstrap_map;  
  private static String _bootstrap_ns;

  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    _reload_allowed = Boolean.valueOf(config.getInitParameter("reload-allowed"));
    
    _bootstrap_ns = config.getInitParameter("bootstrap");
    require(CLAY_ROUTE, _bootstrap_ns);

    prepareResources(config.getServletContext());

    _bootstrap_map =
      (Map)_ns_pubs.invoke(_find_ns.invoke(Symbol.intern(_bootstrap_ns)));

  }

  public void doGet(final HttpServletRequest request,
                    final HttpServletResponse response) {
    process(request, response);
  }

  public void doPost(final HttpServletRequest request,
                     final HttpServletResponse response) {
    process(request, response);
  }

  private void process(final HttpServletRequest request,
                       final HttpServletResponse response) {

    if (null != request.getParameter("reload")) {
      if (_reload_allowed) {
        reload(CLAY_ROUTE, _bootstrap_ns);
        prepareResources(request.getSession().getServletContext());
        _bootstrap_map = (Map)_ns_pubs.invoke(_find_ns.invoke(Symbol.intern(_bootstrap_ns)));
      }
      else
        try {
          response.sendError(405); // method not allowed

        }
        catch (IOException ex) {
          throw new RuntimeException(ex);
        }
    }

    var(CLAY_ROUTE, "service").invoke(_bootstrap_map, request, response);
  }

  private static void require(final String... libs) {
    for (final String lib : libs)
      _require.invoke(Symbol.intern(lib));
  }

  private static void reload(final String... libs) {
    for (final String lib : libs) {
      System.out.format("reloading %s...%n", lib);
      _require.invoke(Symbol.intern(lib), Keyword.intern("reload"));
    }
  }

  private static void prepareResources(final ServletContext ctxt) {
    final Map res = (Map)var(RESOURCES, "RES").get();
    final Set<Map.Entry> s = res.entrySet();
    for (final Map.Entry e : s) {
      final Keyword kd = (Keyword)e.getKey();
      ctxt.setAttribute(kd.getName(), e.getValue());
    }
  }

}
