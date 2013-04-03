; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.ddu
  (:require [org.joyframework.route :reload true :as route]
            [org.joyframework.result :reload true :as rs]
            [org.joyframework.resources :as res]))

(route/defroutes rt org.joyframework.ddu)

(res/load-resources :res 'org.joyframework.ddu)

(defn index [] (rs/tiles "index"))
