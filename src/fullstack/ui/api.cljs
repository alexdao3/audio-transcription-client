(ns fullstack.ui.api
  (:require [ajax.core :as ajax]))

(def api-key
  "API Key. Should normally be made available at run-time via env"
  "24rakMBbFOhlpLYV")

(def api-secret
  "API Secret. Should normally be made available at run-time via env"
  "ejobaTely5Jud4j64cnXp4se2ZP457on")

(def encoded-api-key
  "Construct Basic auth credentials via b64 encoding"
  (str "Basic " (js/btoa (str api-key ":" api-secret))))

(def deepbrain-url
  "Where the magic happens"
  "https://brain.deepgram.com/v2/listen")

(defn request
  "Sets defaults for constructing API requests"
  [{need-auth? :need-auth? :as params}]
  (cond-> (merge {:format (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                    (select-keys params [:keywords?]))}
                 params)
    need-auth?
    (assoc-in [:headers :authorization] encoded-api-key)))
