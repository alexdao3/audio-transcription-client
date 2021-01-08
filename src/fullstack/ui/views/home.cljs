(ns fullstack.ui.views.home
  (:require [clojure.string :as str]
            [goog.object :as gobj]
            [goog.string :as gstr]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [fullstack.ui.styles :refer [colors]]
            [fullstack.ui.views.events :as views.events]
            [fullstack.ui.requests.events :as requests.events]
            [fullstack.ui.requests.subs :as requests.subs]
            [fullstack.ui.transcriptions.subs :as transcriptions.subs]
            [fullstack.ui.views.subs :as views.subs]))

;; TODO: add support for multiple files
(defn file-upload-btn
  "Renders an invisible input element next to a button. Stores a ref to the
  input element to manually trigger clicks for the file upload dialog"
  []
  (let [input-ref (r/atom nil)
        set-ref #(reset! input-ref %)]
    [:<>
     [:input {:type "file"
              :accept "audio/*"
              :multiple true
              :on-change #(rf/dispatch [::views.events/add-audio-file (-> % .-target .-files)])
              :hidden true
              :ref set-ref}]
     [:button.uppercase.p-2.px-8
      {:style {:background (:green colors)}
       :on-click #(.click @input-ref)}
      [:span.text-white "Upload a file"]]]))

(def styles {:first-col  {:flex "0 30%"}
             :second-col {:flex "0 15%"
                          :margin-left "2rem"}
             :third-col  {:flex "0 15%"
                          :margin-left "2rem"}
             :ellipsis-overflow  {:text-overflow "ellipsis"
                                  :overflow "hidden"
                                  :white-space "nowrap"}})

(defn ->view-duration
  "Display audio duration in hh:mm:ss format

  TODO: Parametrize the time format
  TODO: Add tests"
  [total-seconds]
  (let [total-seconds (int total-seconds)
        seconds (mod total-seconds 60)
        total-seconds (- total-seconds seconds)
        minutes (/ (mod total-seconds 3600) 60)
        total-seconds (- total-seconds (* minutes 60))
        hours (/ total-seconds 3600)]
    (if (some pos? [hours minutes seconds])
      (str/join ":" (map #(if (> 10 %)
                            (str "0" %)
                            %)
                         [hours minutes seconds]))
      "Calculating...")))

(defn ->view-file-size
  "Displays file size in MB

  TODO: Parametrize the units (kb, mb, gb, etc)
  TODO: Add tests"
  [bytes]
  (str (gstr/format "%.2f" (/ bytes 1000000)) "MB"))

(def col-headers
  [{:label "Filename" :style (:first-col styles)}
   {:label "Duration" :style (:second-col styles)}
   {:label "Size"     :style (:third-col styles)}])

(defn header
  []
  [:header.flex.justify-center
   [:h1.text-2xl.font-header "Deepgram Audio Server"]])

;; TODO: Mixing a lot of inline styles and tailwind classes here. For
;; readability, consistency, probably better to stick to mostly one approach
;; TODO abstract out a reusable table component
(defn audio-files-table
  []
  (let [{:keys [first-col second-col third-col]} styles]
    [:div.flex-column.mt-8
     [:div.flex {:style {:justify-content "between"
                         :height "100%"}}
      [:div.flex.border-b-2.border-black
       {:style {:width "100%"
                :justify-content "flex-start"}}
       (for [{:keys [label style]} col-headers]
         ^{:key label}
         [:div {:style style}
          [:span.uppercase label]])]]
     (into
      [:<>]
      (map
       (fn [{:keys [blob file]}]
         (let [file-name (gobj/get file "name")]
           ^{:key file-name}
           [:div.flex.py-4.border-b-2.border-gray-400
            {:style {:justify-content "space-between"
                     :position "relative"}}
            [:audio {:id file-name
                     :src blob
                     :ref #(rf/dispatch [::views.events/set-audio-ref file-name %])}]
            [:div.flex {:style {:width "100%"
                                :flex-direction "flex-start"}}
             [:div {:style (merge first-col
                                  (:ellipsis-overflow styles))}
              file-name]
             [:div {:style second-col} (->view-duration (:duration @(rf/subscribe [::transcriptions.subs/metadata file-name])))]
             [:div {:style third-col} (->view-file-size @(rf/subscribe [::views.subs/file-size file-name]))]]
            [:div.flex.pr-4
             {:style {:position "absolute"
                      :right 0}}
             [:button.uppercase.mr-8 {:style {:color (:blue colors)}
                                      :on-click #(views.events/transcribe-file file-name file)}
              "Transcribe"]]]))
       @(rf/subscribe [::views.subs/ordered-audio-files])))]))

;; TODO: Add a state to indicate that the audio file doesn't have a transcript
(defn audio-transcript
  []
  (let [active-file-name @(rf/subscribe [::views.subs/active-transcript-file-name])
        transcript-loading? @(rf/subscribe [::transcriptions.subs/loading? active-file-name])]
    [:div.mt-24
     [:p.uppercase {:style (:ellipsis-overflow styles)}
      (str "Transcript: " @(rf/subscribe [::views.subs/active-transcript-file-name]))]
     [:text-area.block.border-2.border-black.py-8.px-4.mt-2
      {:class (when transcript-loading? "text-gray-600")}
      (cond transcript-loading?
            "We sent a squirrel to gather your transcript. Please await its return..."
            (seq @(rf/subscribe [::views.subs/active-transcript]))
            @(rf/subscribe [::views.subs/active-transcript])
            :else
            nil)]]))

(defn- page
  []
  [:div {:class "h-screen justify-center flex font-sans leading-normal tracking-normal p-4 pt-12"}
   [:div {:class "w-7/12"}
    [header]
    [:div.flex.justify-end.mt-4
     [file-upload-btn]]
    [audio-files-table]
    [audio-transcript]]])

(comment
  @(rf/subscribe [::transcriptions.subs/transcriptions])
  @(rf/subscribe [::views.subs/audio-duration "mixkit-tech-house-vibes-130.mp3"]))
