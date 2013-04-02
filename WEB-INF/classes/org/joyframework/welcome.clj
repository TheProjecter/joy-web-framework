; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.welcome
  (:require [org.joyframework.route :reload true :as route]
            [org.joyframework.result :reload true :as rs]))

(route/defroutes rt org.joyframework.welcome)

(defn index [] (rs/forward "/WEB-INF/jsps/welcome.jsp"))
