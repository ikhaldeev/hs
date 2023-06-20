(ns hs.views
  (:require
    [re-frame.core :as re-frame]
    [hs.state :as state]))

(defn- full-name
  [{:keys [first-name middle-name last-name]}]
  (str first-name " " (when middle-name (str middle-name " ")) last-name))

(defn- patient-item
  [patient]
  [:div {:on-click #(re-frame/dispatch [::state/open-edit-patient-form {:patient-id (:id patient)}])}
   [:div (full-name patient)]
   [:div (:sex patient)]
   [:div (:dob patient)]
   [:div (:address patient)]
   [:div (:policy-number patient)]])

(defn- patients
  []
  (re-frame/dispatch [::state/init-list-patients])
  (fn []
    (let [{:keys [patients _pages]} @(re-frame/subscribe [::state/patients])]
      [:div
       [:h1 "Patients"]
       [:div
        (for [{:keys [id] :as patient} patients]
          ^{:key id}
          [patient-item patient])]
       [:div [:button {:on-click #(re-frame/dispatch [::state/open-create-patient-form])}
              "Create patient"]]])))

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
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :first-name (.. % -target -value)])}]
        (when (:first-name errors)
          [:label (str "Error: " (-> errors :first-name :via))])]
       [:div
        [:label "Middle Name"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :middle-name (.. % -target -value)])}]
        (when (:middle-name errors)
          [:label (str "Error: " (-> errors :middle-name :via))])]
       [:div
        [:label "Last Name"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :last-name (.. % -target -value)])}]
        (when (:last-name errors)
          [:label (str "Error: " (-> errors :last-name :via))])]
       [:div
        [:label "Sex"]
        [:select {:on-change #(re-frame/dispatch [::state/set-form-value :sex (.. % -target -value)])}
         [:option {:value nil} ""]
         [:option {:value "male"} "Male"]
         [:option {:value "female"} "Female"]
         [:option {:value "other"} "Other"]]
        (when (:sex errors)
          [:label (str "Error: " (-> errors :sex :via))])]
       [:div
        [:label "Date of Birth"]
        [:input {:type "date"
                 :on-change #(re-frame/dispatch [::state/set-form-value :dob (.. % -target -value)])}]
        (when (:dob errors)
          [:label (str "Error: " (-> errors :dob :via))])]
       [:div
        [:label "Address"]
        [:textarea {:on-change #(re-frame/dispatch [::state/set-form-value :address (.. % -target -value)])}]
        (when (:address errors)
          [:label (str "Error: " (-> errors :address :via))])]
       [:div
        [:label "Policy Number"]
        [:input {:on-change #(re-frame/dispatch [::state/set-form-value :policy-number (.. % -target -value)])}]
        (when (:policy-number errors)
          [:label (str "Error: " (-> errors :policy-number :via))])]
       [:div
        [:button {:on-click #(re-frame/dispatch [::state/create-patient])}
         "Create"]]])))

(defn- edit-patient
  [params]
  (re-frame/dispatch [::state/init-edit-patient params])
  (fn [_params]
    (let [form-data @(re-frame/subscribe [::state/form-data])
          errors @(re-frame/subscribe [::state/form-errors])]
      [:div
       [:div [:button {:on-click #(re-frame/dispatch [::state/cancel-edit-patient])}
              "Back"]]
       [:div
        [:label "First Name"]
        [:input {:value (:first-name form-data)
                 :on-change #(re-frame/dispatch [::state/set-form-value :first-name (.. % -target -value)])}]
        (when (:first-name errors)
          [:label (str "Error: " (-> errors :first-name :via))])]
       [:div
        [:label "Middle Name"]
        [:input {:value (:middle-name form-data)
                 :on-change #(re-frame/dispatch [::state/set-form-value :middle-name (.. % -target -value)])}]
        (when (:middle-name errors)
          [:label (str "Error: " (-> errors :middle-name :via))])]
       [:div
        [:label "Last Name"]
        [:input {:value (:last-name form-data)
                 :on-change #(re-frame/dispatch [::state/set-form-value :last-name (.. % -target -value)])}]
        (when (:last-name errors)
          [:label (str "Error: " (-> errors :last-name :via))])]
       [:div
        [:label "Sex"]
        [:select {:value (:sex form-data)
                  :on-change #(re-frame/dispatch [::state/set-form-value :sex (.. % -target -value)])}
         [:option {:value nil} ""]
         [:option {:value "male"} "Male"]
         [:option {:value "female"} "Female"]
         [:option {:value "other"} "Other"]]
        (when (:sex errors)
          [:label (str "Error: " (-> errors :sex :via))])]
       [:div
        [:label "Date of Birth"]
        [:input {:type "date"
                 :value (:dob form-data)
                 :on-change #(re-frame/dispatch [::state/set-form-value :dob (.. % -target -value)])}]
        (when (:dob errors)
          [:label (str "Error: " (-> errors :dob :via))])]
       [:div
        [:label "Address"]
        [:textarea {:value (:address form-data)
                    :on-change #(re-frame/dispatch [::state/set-form-value :address (.. % -target -value)])}]
        (when (:address errors)
          [:label (str "Error: " (-> errors :address :via))])]
       [:div
        [:label "Policy Number"]
        [:input {:value (:policy-number form-data)
                 :on-change #(re-frame/dispatch [::state/set-form-value :policy-number (.. % -target -value)])}]
        (when (:policy-number errors)
          [:label (str "Error: " (-> errors :policy-number :via))])]
       [:div
        [:button {:on-click #(re-frame/dispatch [::state/edit-patient])}
         "Save"]]])))

(defn main-panel
  []
  (let [{:keys [route-name params]} @(re-frame/subscribe [::state/active-route])]
    (case route-name
      :create-patient [create-patient params]
      :edit-patient [edit-patient params]
      [patients params])))
