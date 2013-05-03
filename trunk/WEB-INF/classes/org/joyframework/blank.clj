; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.blank
  (:require [org.joyframework.route :reload true :as route]
            [org.joyframework.config :reload true :as conf]            
            [org.joyframework.result :reload true :as rs]))

(conf/set :routes (route/defroutes 'org.joyframework.blank))

(defn index [] (rs/ok "Being Joyful!"))

(defn hello [] (rs/tiles "hello"))
