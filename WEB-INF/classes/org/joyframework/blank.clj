; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.blank
  (:require [org.joyframework.route :reload true :as route]
            [org.joyframework.result :reload true :as rs]))

(route/defroutes __jf_rt__ org.joyframework.blank)

(defn index [] (rs/ok "Being Joyful!"))

(defn hello [] (rs/tiles "hello"))
