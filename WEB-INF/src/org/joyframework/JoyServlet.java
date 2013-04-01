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

  private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

  @SuppressWarnings("compatibility:7579876346806444927")
  private static final long serialVersionUID = 1L;

  

  /**
   *
   */
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    _bootstrap = config.getInitParameter("bootstrap");

    require(CLAY_ROUTE, _bootstrap);

    prepareResources(config.getServletContext());

    _m = (Map)_nsPubs.invoke(_findNs.invoke(Symbol.intern(_bootstrap)));
    
    _reloadAllowed =
      Boolean.valueOf(config.getInitParameter("reload-allowed"));
  }

  /**
   *
   */
  public void doGet(final HttpServletRequest request,
                    final HttpServletResponse response) {
    process(request, response);
  }

  /**
   *
   */
  public void doPost(final HttpServletRequest request,
                     final HttpServletResponse response) {
    process(request, response);
  }

  /**
   *
   */
  private void process(final HttpServletRequest request,
                       final HttpServletResponse response) {

    if (null != request.getParameter("reload")) {
      if (_reloadAllowed) {
        reload(CLAY_ROUTE, _bootstrap);
        prepareResources(request.getSession().getServletContext());
        _m = (Map)_nsPubs.invoke(_findNs.invoke(Symbol.intern(_bootstrap)));
      }
      else
        try {
          response.sendError(405); // method not allowed

        }
        catch (IOException ex) {
          throw new RuntimeException(ex);
        }
    }

    var(CLAY_ROUTE, "service").invoke(_m, request, response);
  }


  /**
   *
   */
  private static void require(final String... libs) {
    for (final String lib : libs)
      _require.invoke(Symbol.intern(lib));
  }

  /**
   *
   */
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
      //System.out.println(kd.getName());
      ctxt.setAttribute(kd.getName(), e.getValue());
    }
  }

  //private Var _rt;

  /**
   *
   */
  private final static IFn _require = var("clojure.core", "require");

  private final static IFn _findNs = var("clojure.core", "find-ns");

  private final static IFn _nsPubs = var("clojure.core", "ns-publics");

  private static String _bootstrap;

  private final static String
    CLAY_ROUTE = "org.joyframework.route",
    RESOURCES = "org.joyframework.resources";

  private static boolean _reloadAllowed = false;

  private Map _m;
}
