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
  ::edit-patient
  (fn [{:keys [_db]} [_ {:keys [patient-id data]} {:keys [on-success on-failure]}]]
    {:http-xhrio {:method          :put
                  :uri             (str "/api/patients/" patient-id)
                  :params          data
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      on-success
                  :on-failure      on-failure}}))

(re-frame/reg-event-fx
  ::delete-patient
  (fn [{:keys [_db]} [_ {:keys [patient-id]} {:keys [on-success on-failure]}]]
    {:http-xhrio {:method          :delete
                  :uri             (str "/api/patients/" patient-id)
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      on-success
                  :on-failure      on-failure}}))

(re-frame/reg-event-fx
  ::list-patients
  (fn [{:keys [_db]} [_ params {:keys [on-success on-failure]}]]
    {:http-xhrio {:method          :get
                  :uri             "/api/patients"
                  :params          params
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      on-success
                  :on-failure      on-failure}}))

(re-frame/reg-event-fx
  ::load-patient
  (fn [{:keys [_db]} [_ {:keys [patient-id]} {:keys [on-success on-failure]}]]
    {:http-xhrio {:method          :get
                  :uri             (str "/api/patients/" patient-id)
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      on-success
                  :on-failure      on-failure}}))
