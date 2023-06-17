(ns hs.state
  (:require
    [re-frame.core :as re-frame]
    [day8.re-frame.http-fx]
    [ajax.core :refer [json-request-format json-response-format]]))

(re-frame/reg-sub
  ::active-route
  (fn [db]
    (:active-route db)))

(re-frame/reg-event-fx
  ::set-active-route
  (fn [{:keys [db]} [_ route]]
    {:db (assoc db :active-route route)}))

(re-frame/reg-event-fx
  ::init-create-patient-form
  (fn [{:keys [db]} [_]]
    {:db (assoc db :form {})}))

(re-frame/reg-event-fx
  ::set-form-value
  (fn [{:keys [db]} [_ field event]]
    (let [value (.. event -target -value)]
      {:db (assoc-in db [:form field] value)})))

(re-frame/reg-event-fx
  ::create-patient
  (fn [{:keys [db]} [_]]
    (let [form-data (:form db)]
      {:db (-> db
               (dissoc :form-errors))
       :http-xhrio {:method          :post
                    :uri             "/api/patients"
                    :params          form-data
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [::create-patient-success]
                    :on-failure      [::create-patient-failure]}})))

(re-frame/reg-event-fx
  ::create-patient-success
  (fn [{:keys [db]} [_ {patient-id :id}]]
    {:db (assoc-in db [:form] {})}))

(re-frame/reg-event-fx
  ::create-patient-failure
  (fn [{:keys [db]} [_ {{errors :errors} :response}]]
    (let [prepared (->> errors
                        (map (juxt #(-> % :field keyword) identity))
                        (into {}))]
      {:db (assoc-in db [:form-errors] prepared)})))

(re-frame/reg-sub
  ::form-errors
  (fn [db]
    (:form-errors db)))

(comment
  @re-frame.db/app-db

  (re-frame/dispatch [::set-active-route {:route-name :create-patient}])
  @(re-frame/subscribe [::form-errors])

  (-> @re-frame.db/app-db
      (get :form)))
