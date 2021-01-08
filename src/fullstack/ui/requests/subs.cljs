(ns fullstack.ui.requests.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::request-in-progress?
 (fn [db [_ resource-name]]
   (->> (get-in db [:requests resource-name :load-status])
        vals
        (some #(= :requests/loading %)))))

(rf/reg-sub
 ::data
 (fn [db [_ resource]]
   (get-in db [:requests resource])))

(rf/reg-sub
 ::contents
 (fn [[_ hello]]
   (rf/subscribe [::data hello]))
 (fn [data]
   (get data :contents)))

(rf/reg-sub
 ::single-resource
 (fn [[_ resource]]
   (rf/subscribe [::contents resource]))
 (fn [resources [_ resource-id]]
   (get resources resource-id)))

(rf/reg-sub
 ::ordered-ids
 (fn [[_ resource]]
   (rf/subscribe [::data resource]))
 (fn [data]
   (get data :ordered)))

(rf/reg-sub
 ::ordered-resources
 (fn [[_ resource]]
   [(rf/subscribe [::contents resource])
    (rf/subscribe [::ordered-ids resource])])
 (fn [[contents ordered-ids]]
   (mapv #(-> (get contents %)
              :children
               first
               :data)
         ordered-ids)))

(comment (rf/subscribe [::request-in-progress? :subreddits])
         @(rf/subscribe [::ordered-resources :subreddits]))
