(ns hs.core
  (:require
    [hs.handler :as handler]
    [hs.db :as db]
    [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& _args]
  (db/init!)
  (run-jetty handler/handler {:port 3000
                              :join? false}))
