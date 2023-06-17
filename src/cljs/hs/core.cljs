(ns hs.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as re-frame]
            [hs.views :as views]))

(defn dev-setup []
  (when goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rd/render
   [views/main-panel]
   (.getElementById js/document "app")))

(defn ^:export init
  []
  (dev-setup)
  (mount-root))

(comment
  (js/console.log "test"))
