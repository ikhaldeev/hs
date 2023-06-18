(ns hs.views
  (:require
    [re-frame.core :as re-frame]
    [hs.state :as state]
                                        ;    [hs.create-patient.views :as create-patient]
    ))

(defn- patients
  [params]
  [:div
   [:h1 "Patients"]
   [:div [:button {:on-click #(re-frame/dispatch [::state/open-create-patient-form])}
          "Create patient"]]])

(defn- create-patient
  []
  (re-frame/dispatch [::state/init-create-patient-form])
  (fn []
    (let [errors @(re-frame/subscribe [::state/form-errors])]
      [:div
       [:div [:button {:on-click #(re-frame/dispatch [::state/cancel-create-patient])}
              "Back"]]
       [:div
        [:label "First Name"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :first-name %])}]
        (when (:first-name errors)
          [:label (str "Error: " (-> errors :first-name :via))])]
       [:div
        [:label "Middle Name"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :middle-name %])}]
        (when (:middle-name errors)
          [:label (str "Error: " (-> errors :middle-name :via))])]
       [:div
        [:label "Last Name"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :last-name %])}]
        (when (:last-name errors)
          [:label (str "Error: " (-> errors :last-name :via))])]
       [:div
        [:label "Sex"]
        [:select {:on-change #(re-frame/dispatch [::state/set-form-value :sex %])}
         [:option {:value nil} ""]
         [:option {:value "male"} "Male"]
         [:option {:value "female"} "Female"]
         [:option {:value "other"} "Other"]]
        (when (:sex errors)
          [:label (str "Error: " (-> errors :sex :via))])]
       [:div
        [:label "Date of Birth"]
        [:input {:type "date"
                 :on-change #(re-frame/dispatch [::state/set-form-value :dob %])}]
        (when (:dob errors)
          [:label (str "Error: " (-> errors :dob :via))])]
       [:div
        [:label "Address"]
        [:textarea {:on-change #(re-frame/dispatch [::state/set-form-value :address %])}]
        (when (:address errors)
          [:label (str "Error: " (-> errors :address :via))])]
       [:div
        [:label "Policy Number"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :policy-number %])}]
        (when (:policy-number errors)
          [:label (str "Error: " (-> errors :policy-number :via))])]
       [:div
        [:button {:on-click #(re-frame/dispatch [::state/create-patient])}
         "Create"]]])))

(defn- edit-patient
  [params]
  [:div "not implemented"])

(defn main-panel
  []
  (let [{:keys [route-name params]} @(re-frame/subscribe [::state/active-route])]
    (case route-name
      :create-patient [create-patient params]
      :edit-patient [edit-patient params]
      [patients params])))
