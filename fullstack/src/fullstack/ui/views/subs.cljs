(ns fullstack.ui.views.subs
  (:require [fullstack.ui.requests.subs :as requests.subs]
            [goog.object :as gobj]
            [re-frame.core :as rf]
            [fullstack.ui.transcriptions.subs :as transcriptions.subs]))

(defn audio-elem->duration
  [audio-elem]
  (gobj/get audio-elem "duration"))

(rf/reg-sub
 ::ordered-audio-file-names
 (fn [db]
   (print (get-in db [:views :home :audio :ordered-ids]))
   (get-in db [:views :home :audio :ordered-ids])))

(rf/reg-sub
 ::audio-files
 (fn [db]
   (get-in db [:views :home :audio :contents])))

(rf/reg-sub
 ::audio-file
 (fn [db [_ file-name]]
   (get-in db [:views :home :audio :contents file-name])))

(rf/reg-sub
 ::ordered-audio-files
 (fn []
   [(rf/subscribe [::ordered-audio-file-names])
    (rf/subscribe [::audio-files])])
 (fn [[file-names audio-files]]
   (map (fn [file-name] (get audio-files file-name)) file-names)))

(rf/reg-sub
 ::audio-ref
 (fn [db [_ file-name]]
   (js/console.log file-name)
   (get-in db [:views :home :audio :contents file-name :ref])))

(rf/reg-sub
 ::audio-duration
 (fn [[_ file-name]]
   (rf/subscribe [::audio-ref file-name]))
 (fn [audio-elem]
   (print "Aduio file" audio-elem)
   (js/console.log audio-elem)
   (js/console.log (some-> audio-elem (.-duration)))
   (gobj/get audio-elem "duration")))

(rf/reg-sub
 ::file-size
 (fn [[_ file-name]]
   (rf/subscribe [::audio-file file-name]))
 (fn [audio-file]
   (-> audio-file
       :file
       (gobj/get "size"))))

(rf/reg-sub
 ::active-transcript-file-name
 (fn [db _]
   (get-in db [:app :active-transcript-file-name])))

(rf/reg-sub
 ::active-transcript
 (fn []
   [(rf/subscribe [::active-transcript-file-name])
    (rf/subscribe [::transcriptions.subs/transcriptions])])
 (fn [[file-name transcriptions]]
   (transcriptions.subs/get-transcript (get-in transcriptions [file-name :data]))))

(comment
  (-> @(rf/subscribe [::audio-files])
      first
      :file
      (gobj/get "name")))
