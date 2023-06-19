(ns hs.state.api
  (:require
    [re-frame.core :as re-frame]
    [day8.re-frame.http-fx]
    [ajax.core :refer [json-request-format json-response-format]]))

(re-frame/reg-event-fx
  ::create-patient
  (fn [{:keys [_db]} [_ data {:keys [on-success on-failure]}]]
    {:http-xhrio {:method          :post
                  :uri             "/api/patients"
                  :params          data
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      on-success
                  :on-failure      on-failure}}))

(re-frame/reg-event-fx
  ::list-patients
  (fn [{:keys [_db]} [_ {:keys [on-success on-failure]}]]
    {:http-xhrio {:method          :get
                  :uri             "/api/patients"
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      on-success
                  :on-failure      on-failure}}))
