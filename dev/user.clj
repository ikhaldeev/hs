(ns user
  (:require [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as server]
            [ring.adapter.jetty :refer [run-jetty]]
            [hs.handler :refer [dev-handler]]))

(def server (run-jetty dev-handler {:port 3000
                                    :join? false}))
(defn cljs-repl
  "Connects to a given build-id. Defaults to `:app`."
  ([]
   (cljs-repl :app))
  ([build-id]
   (server/start!)
   (shadow/watch build-id)
   (shadow/nrepl-select build-id)))

(comment
  (.stop server))
