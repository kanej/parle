(ns nrepl-node-client.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.nodejs :as nodejs]
              [cljs.core.async :refer [put! chan <!]]

              [nrepl-node-client.net :as net]
              [nrepl-node-client.nrepl :as nrepl]))

(nodejs/enable-util-print!)

(def readline (nodejs/require "readline"))
(def fs (nodejs/require "fs"))

;; nrepl


(defn str->int
  [s]
  (when (re-matches #"^\d+$" s)
    (js/parseInt s)))

(defn read-file [file-name]
  (let [file-ch (chan)]
    (.readFile fs file-name (fn [err data] (put! file-ch (-> data .toString str->int))))
    file-ch))

(defn read-user-input [term read-ch]
  (.question term "=> " #(put! read-ch %)))

(defn- terminal []
  (.createInterface readline #js {:input (.-stdin js/process) :output (.-stdout js/process)}))

(defn setup-repl []
  (let [read-ch (chan)
        eval-ch (chan)
        eval-result-ch (chan)
        term (terminal)]
    (go
     (loop []
       (let [expr (<! read-ch)]
         (if (= expr "exit")
           (.exit js/process)
           (let [eval-ch (put! eval-ch expr)
                 result (<! eval-result-ch)]
             (println (.-value result))

             (read-user-input term read-ch)
             (recur))))))
    (go
      (let [repl-port (<! (read-file ".nrepl-port"))
            nrepl-client (nrepl/connect repl-port)]
        (println "Node REPL client connected to NREPL at localhost on port " repl-port)
        (read-user-input term read-ch)
        (loop [expr (<! eval-ch)]
          (nrepl/perform-op nrepl-client {:op "eval" :code expr} #(put! eval-result-ch %))
          (recur (<! eval-ch)))))))

(defn -main [& args]
  (setup-repl))

(set! *main-cli-fn* -main)
