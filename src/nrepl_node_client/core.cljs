(ns nrepl-node-client.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.nodejs :as nodejs]
              [cljs.core.async :refer [put! chan <!]]
              [nrepl-node-client.bencode :as bencode]))

(nodejs/enable-util-print!)

(def readline (nodejs/require "readline"))
(def blessed (nodejs/require "blessed"))
(def fs (nodejs/require "fs"))

;; nrepl

(def net (nodejs/require "net"))


(defn str->int
  [s]
  (when (re-matches #"^\d+$" s)
    (js/parseInt s)))

(defn read-file [file-name]
  (let [file-ch (chan)]
    (.readFile fs file-name (fn [err data] (put! file-ch (-> data .toString str->int))))
    file-ch))

(defn nrepl-connect [port]
  (let [client-ch (chan)
        client (.connect net #js {:port port})]
    (put! client-ch client)
    client-ch))

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
            client (<! (nrepl-connect repl-port))]
        (println "Node REPL client connected to NREPL at localhost on port " repl-port)
        (read-user-input term read-ch)
        (loop []
          (let [expr (<! eval-ch)
                encoded-expr (bencode/encode expr)]
            (.once client "data" #(put! eval-result-ch (-> % bencode/decode)))
            (.write client encoded-expr)
            (recur)))))2))

(defn -main [& args]
  (setup-repl))

(set! *main-cli-fn* -main)
