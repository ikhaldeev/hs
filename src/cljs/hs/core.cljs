(ns hs.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as re-frame]))

(defn dev-setup []
  (when goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rd/render
   [:div "re-mounted"]
   (.getElementById js/document "app")))

(defn ^:export init
  []
  (dev-setup)
  (mount-root))
