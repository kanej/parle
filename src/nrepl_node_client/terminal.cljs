(ns nrepl-node-client.terminal
    (:require [cljs.nodejs :as nodejs]))

(def readline (nodejs/require "readline"))

(defprotocol Terminal
  (read-user-input [this callback-fn]))

(defrecord Term [rd]
  Terminal
  (read-user-input [this callback-fn]
    (.question rd "=> " callback-fn)))

(defn new-terminal []
  (let [options #js {:input (.-stdin js/process) :output (.-stdout js/process)}
        rd (.createInterface readline options)]
    (->Term rd)))
