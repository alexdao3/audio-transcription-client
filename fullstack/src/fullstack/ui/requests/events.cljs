(ns fullstack.ui.requests.events
  (:require
   day8.re-frame.http-fx
   [fullstack.ui.api :as api]
   [re-frame.core :as rf]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Init App
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; ARRAY BUFFER VS BINARY STRINg


(rf/reg-event-fx
 ::transcribe-audio
 (fn [{:keys [db]} [_ file-name blob {model :model}]]
   (let [file-transcribed? (get-in db [:requests :transcriptions file-name :data])
         model (or model "general")]
     (if file-transcribed?
       {:db (assoc-in db [:app :active-transcript-file-name] file-name)}
       {:http-xhrio (api/request
                     {:uri api/deepbrain-url
                      :method :post
                      :need-auth? true
                      ;; TODO: read this from the file object we already have.
                      ;; use :type
                      :format {:content-type "audio/mpeg"
                               :write identity}
                      :params blob
                               ;; :punctuate true
                      :keywords? true
                      :on-success [::transcribe-audio.success file-name]
                      :on-failure [::transcribe-audio.failure file-name]})
        :db (-> db
                (assoc-in [:app :active-transcript-file-name] file-name)
                (assoc-in [:requests :transcriptions file-name] {:status :loading
                                                                 :error nil}))}))))

(rf/reg-event-db
 ::transcribe-audio.success
 (fn [db [_ file-name resp]]
   (let [resp (update-in resp [:metadata :created] js/Date.)]
     (print resp)
     (-> db
         (assoc-in [:requests :transcriptions file-name] {:status :succeeded
                                                          :data resp
                                                          :error nil})))))

(rf/reg-event-db
 ::transcribe-audio.failure
 (fn [db [_ file-name model resp]]
   (assoc-in db [:requests :transcriptions file-name] {:status :failed
                                                       :error resp})))
