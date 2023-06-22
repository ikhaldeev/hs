(ns hs.core
  (:require
    [hs.handler :as handler]
    [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& _args]
  (run-jetty handler/handler {:port 3000
                              :join? false}))
