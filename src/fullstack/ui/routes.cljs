(ns fullstack.ui.routes
  (:require
   [re-frame.core :as rf]
   [reitit.coercion.malli]
   [reitit.frontend]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend.controllers :as rfc]
   [fullstack.ui.views.home :as home]))

;;; Subs

(rf/reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

;;; Events

(rf/reg-event-fx
 :navigate
 (fn [_cofx [_ & route]]
   {:navigate! route}))

;; Triggering navigation from events.
(rf/reg-fx
 :navigate!
 (fn [route]
   (apply rfe/push-state route)))

(rf/reg-event-db
 :navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

;;; Routes

(def routes
  ["/"
   [""
    {:name      :routes/home
     :view      home/page
     :link-text "Home"
     :controllers
     [{:start (fn []
                (println "Entering home page"))
       :stop  (fn []
                (println "Leaving home page"))}]}]])

(def router
  (reitit.frontend/router
   routes
    ;; {:data {:coercion reitit.coercion.malli/coercion}}
   ))

(defn on-navigate [new-match]
  (when new-match
    (rf/dispatch [:navigated new-match])))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))
