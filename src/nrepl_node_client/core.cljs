(ns nrepl-node-client.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.nodejs :as nodejs]
              [cljs.core.async :refer [put! chan <!]]
              [cljs.pprint :refer [pprint]]

              [nrepl-node-client.config :refer [read-file]]
              [nrepl-node-client.net :as net]
              [nrepl-node-client.nrepl :as nrepl]
              [nrepl-node-client.terminal :refer [new-terminal read-user-input]]))

(def *debug* false)

(def version "0.0.1")

(nodejs/enable-util-print!)

(defn- read-repl-port []
  (let [repl-port-ch (chan)]
    (read-file ".nrepl-port" #(put! repl-port-ch %))
    repl-port-ch))

(defn- async-perform-op [nrepl-client op]
  (let [result-chan (chan)]
    (nrepl/perform-op nrepl-client op #(put! result-chan %))
    result-chan))

(defn- print-intro [server-description repl-port]
  (let [nrepl-version (aget server-description "versions" "nrepl" "version-string")
        clj-version (aget server-description "versions" "clojure" "version-string")
        java-version (aget server-description "versions" "java" "version-string")]
    ;;(.log js/console server-description)
    (println "Connecting to nREPL at localhost on port" repl-port)
    (println "node-nrepl-client" version ", nREPL" nrepl-version)
    (println "Clojure" clj-version)
    (println "Java" java-version)
    (println "    Exit: (exit) or (quit)")
    (println "")))

(defn setup-repl []
  (let [read-ch (chan)
        eval-ch (chan)
        eval-result-ch (chan)
        terminal (new-terminal)
        rui (fn [] (read-user-input terminal #(put! read-ch %)))]
    (go
     (loop []
       (let [result (<! eval-result-ch)]
         (when *debug* (.log js/console result))
         (when-let [token (or (aget result "out") (aget result "value"))] (print token))
         (when (aget result "value") (rui))
         (recur))))
    (go
      (loop []
        (let [expr (<! read-ch)]
         (if (or (= expr "exit") (= expr "quit") (= expr "(exit)") (= expr "(quit)"))
           (do
             (println "Bye for now!")
             (.exit js/process))
           (do
             (put! eval-ch expr)
             (recur))))))
    (go
      (let [repl-port (<! (read-repl-port))
            nrepl-client (nrepl/connect repl-port)
            server-description (<! (async-perform-op nrepl-client {:op "describe"}))]
        (print-intro server-description repl-port)
        (rui)
        (loop [expr (<! eval-ch)]
          (nrepl/perform-op nrepl-client {:op "eval" :code expr} #(put! eval-result-ch %))
          (recur (<! eval-ch)))))))

(defn -main [& args]
  (setup-repl))

(set! *main-cli-fn* -main)
