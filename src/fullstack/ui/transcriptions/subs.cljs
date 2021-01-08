(ns fullstack.ui.transcriptions.subs
  (:require
   [re-frame.core :as rf]))

(defn get-transcript
  [transcription]
  (-> transcription
      ;; :data
      :results
      :channels
      first
      :alternatives
      first
      :transcript))

(rf/reg-sub
 ::transcriptions
 (fn [db]
   (get-in db [:requests :transcriptions])))

(rf/reg-sub
 ::transcription
 (fn [db [_ file-name]]
   (get-in db [:requests :transcriptions file-name])))

(rf/reg-sub
 ::metadata
 (fn [db [_ file-name]]
   (get-in db [:requests :transcriptions file-name :data :metadata])))

(rf/reg-sub
 ::loading?
 (fn [[_ file-name]]
   (rf/subscribe [::transcription file-name]))
 (fn [transcription]
   (= :loading (:status transcription))))

(rf/reg-sub
 ::transcripts
 (fn [db [_]]
   (->> (get-in db [:requests :transcriptions])
        (map #(-> %
                  second
                  :data
                  :results
                  :channels
                  first
                  :alternatives
                  first
                  :transcript)))))

(rf/reg-sub
 ::transcript
 (fn [db [_ file-name]]
   (-> (get-in db [:requests :transcriptions file-name])
       :data
       :results
       :channels
       first
       :alternatives
       first
       :transcript)))

(rf/reg-sub
 ::most-recent-transcript
 (fn []
   (rf/subscribe [::transcriptions]))
 (fn [transcriptions]
   (let [most-recent-transcription
         (reduce
          (fn [most-recent next]
            (if (> (get-in most-recent [:metadata :created])
                   (get-in next [:metadata :created]))
              most-recent
              next))
          (map (comp :data second) transcriptions))]
     (get-transcript most-recent-transcription))))

(comment
  (-> @(rf/subscribe [::transcription-metadata "spkr0.wav"])
      :data
      :results
      :channels
      first
      :alternatives
      first)
  (-> (map (comp :metadata :data second) @(rf/subscribe [::transcriptions]))
      first)
  second

  (map (comp second) @(rf/subscribe [::most-recent-transcript]))
  (-> @(rf/subscribe [::transcription-metadata "spkr0.wav"])
      :created
      (js/Date.)))

{:file-name {:general {}
             :phonecall {}}}
