(ns hs.handler
  (:require
    [clojure.walk :refer [keywordize-keys]]
    [compojure.core :refer [defroutes context GET PUT POST DELETE]]
    [compojure.route :refer [not-found resources]]
    [compojure.coercions :refer [as-int]]
    [ring.util.response :refer [resource-response response bad-request status]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [ring.adapter.jetty :refer [run-jetty]]
    [hs.patients :as patients]))

(defroutes pages
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/")
  (not-found "Not found"))

(defroutes api
  (context "/api/patients" []
    (POST "/" {body :body}
      (-> (patients/create-patient body)
          response))
    (GET "/:id" [id :<< as-int]
      (if-let [result (patients/get-patient id)]
        (response result)
        (status 404)))
    (PUT "/:id" [id :<< as-int :as request]
      (-> (patients/edit-patient id (:body request))
          response))
    (DELETE "/:id" [id :<< as-int]
      (patients/delete-patient id)
      (status 204))
    (GET "/" {params :query-params}
      (-> (patients/list-patients (keywordize-keys params))
          response))))

(defroutes app
  api
  pages)

(defn wrap-exceptions
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (bad-request (ex-data e))))))

(def dev-handler
  (-> #'app
      wrap-reload
      wrap-exceptions
      (wrap-json-body {:keywords? true})
      wrap-params
      wrap-json-response))

(def handler
  (-> #'app
      wrap-exceptions
      (wrap-json-body {:keywords? true})
      wrap-params
      wrap-json-response))

(comment
  (def server (run-jetty dev-handler {:port 3000
                                      :join? false}))
  (.stop server))
