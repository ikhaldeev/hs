(ns hs.state
  (:require
    [re-frame.core :as re-frame]
    [hs.state.api :as api]))

(re-frame/reg-sub
  ::active-route
  (fn [db]
    (:active-route db)))

(re-frame/reg-event-fx
  ::set-active-route
  (fn [{:keys [db]} [_ route]]
    {:db (assoc db :active-route route)}))

(re-frame/reg-event-fx
  ::open-create-patient-form
  (fn [{:keys [_db]} [_]]
    {:dispatch [::set-active-route {:route-name :create-patient}]}))

(re-frame/reg-event-fx
  ::cancel-create-patient
  (fn [{:keys [_db]} [_]]
    {:dispatch [::set-active-route {:route-name :patients}]}))

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
               (assoc :loading true)
               (dissoc :form-errors))
       :dispatch [::api/create-patient
                  form-data
                  {:on-success [::create-patient-success]
                   :on-failure [::create-patient-failure]}]})))

(re-frame/reg-event-fx
  ::create-patient-success
  (fn [{:keys [db]} [_ {patient-id :id}]]
    {:db (-> db
             (assoc :loading false)
             (assoc :form {}))}))

(re-frame/reg-event-fx
  ::create-patient-failure
  (fn [{:keys [db]} [_ {{errors :errors} :response}]]
    (let [prepared (->> errors
                        (map (juxt #(-> % :field keyword) identity))
                        (into {}))]
      {:db (-> db
               (assoc :loading false)
               (assoc :form-errors prepared))})))

(re-frame/reg-sub
  ::form-errors
  (fn [db]
    (:form-errors db)))

(re-frame/reg-event-fx
  ::init-list-patients
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :loading true))
     :dispatch [::api/list-patients
                {:on-success [::list-patients-success]
                 :on-failure [::list-patients-failure]}]}))

(re-frame/reg-event-fx
  ::list-patients-success
  (fn [{:keys [db]} [_ result]]
    {:db (-> db
             (assoc :loading false)
             (assoc :patients result))}))

(re-frame/reg-event-fx
  ::list-patients-failure
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :loading false))}))

(re-frame/reg-sub
  ::patients
  (fn [db]
    (:patients db)))

(comment
  @re-frame.db/app-db

  (re-frame/dispatch [::set-active-route {:route-name :create-patient}])
  @(re-frame/subscribe [::form-errors])

  (-> @re-frame.db/app-db
      (get :form)))
