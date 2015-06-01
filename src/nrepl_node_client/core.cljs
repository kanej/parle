(ns nrepl-node-client.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.nodejs :as nodejs]
              [cljs.core.async :refer [put! chan <!]]

              [nrepl-node-client.config :refer [read-file]]
              [nrepl-node-client.net :as net]
              [nrepl-node-client.nrepl :as nrepl]
              [nrepl-node-client.terminal :refer [new-terminal read-user-input]]))

(nodejs/enable-util-print!)

(defn- read-repl-port []
  (let [repl-port-ch (chan)]
    (read-file ".nrepl-port" #(put! repl-port-ch %))
    repl-port-ch))

(defn setup-repl []
  (let [read-ch (chan)
        eval-ch (chan)
        eval-result-ch (chan)
        terminal (new-terminal)]
    (go
     (loop []
       (let [expr (<! read-ch)]
         (if (= expr "exit")
           (.exit js/process)
           (let [eval-ch (put! eval-ch expr)
                 result (<! eval-result-ch)]
             (println (.-value result))

             (read-user-input terminal #(put! read-ch %))
             (recur))))))
    (go
      (let [repl-port (<! (read-repl-port))
            nrepl-client (nrepl/connect repl-port)]
        (println "Node REPL client connected to NREPL at localhost on port " repl-port)
        (read-user-input terminal #(put! read-ch %))
        (loop [expr (<! eval-ch)]
          (nrepl/perform-op nrepl-client {:op "eval" :code expr} #(put! eval-result-ch %))
          (recur (<! eval-ch)))))))

(defn -main [& args]
  (setup-repl))

(set! *main-cli-fn* -main)
