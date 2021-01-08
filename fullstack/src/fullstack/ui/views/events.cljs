(ns fullstack.ui.views.events
  (:require
   day8.re-frame.http-fx
   [fullstack.ui.api :as api]
   [fullstack.ui.db :as db]
   [goog.object :as gobj]
   [re-frame.core :as rf]
   [fullstack.ui.requests.events :as requests.events]))

(defn create-audio-blob
  [file]
  (js/URL.createObjectURL file))

(defn read-file
  [file-name blob]
  (rf/dispatch [::requests.events/transcribe-audio file-name blob]))

(defn transcribe-file
  [file-name file]
  (-> (.arrayBuffer file)
      (.then (partial read-file file-name))))

(rf/reg-fx
 ::transcribe-file
 (fn [[file-name file]]
   (transcribe-file file-name file)))

(rf/reg-event-fx
 ::add-audio-file
 (fn [{:keys [db]} [_ files]]
   (print "FILES" files)
   (let [file (gobj/get files "0")
         file-name (gobj/get file "name")]
     (print "DEBUG:" file file-name)
     {:db
      (-> db
          (update-in
           [:views :home :audio :ordered-ids]
           #(conj (or % []) file-name))
          (assoc-in [:views :home :audio :contents file-name]
                    {:file file
                     :blob (create-audio-blob file)}))
      ::transcribe-file [file-name file]})))

(rf/reg-event-db
 ::set-audio-ref
 (fn [db [_ file-name ref]]
   (assoc-in db [:views :home :audio :contents file-name :ref] ref)))
