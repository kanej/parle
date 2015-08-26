(ns parle.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.nodejs :as nodejs]
              [cljs.core.async :refer [put! chan <!]]
              [cljs.pprint :refer [pprint]]

              [parle.config :refer [read-nrepl-port-file args->map]]
              [parle.net :as net]
              [parle.nrepl :as nrepl]
              [parle.terminal :refer [new-terminal read-user-input]]))

(def *debug* false)
(def version "0.2.0")

(defonce current-ns (atom 'user))
(defonce current-session (atom nil))

(nodejs/enable-util-print!)

(defn- resolve-port [{:keys [port]}]
  (let [repl-port-ch (chan)]
    (if (not (nil? port))
      (put! repl-port-ch port)
      (read-nrepl-port-file (fn [port]
                              (when-not port
                                (println "Please specify a port, or run parle in a directory with a .nrepl-port file.")
                                (.exit js/process))
                              (put! repl-port-ch port))))
    repl-port-ch))

(defn- new-session [nrepl-client]
  (let [result-chan (chan)]
    (nrepl/perform-op nrepl-client {:op "clone"} #(put! result-chan (aget % "new-session")))
    result-chan))

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
    (println (str "parle " version ", nREPL " nrepl-version))
    (println "Clojure" clj-version)
    (println "Java" java-version)
    (println "    Exit: (exit) or (quit)")
    (println "")))

(defn- prompt []
  (str @current-ns "=> "))

(defn setup-repl [options]
  (let [read-ch (chan)
        eval-ch (chan)
        eval-result-ch (chan)
        terminal (new-terminal)
        rui (fn [] (read-user-input terminal #(put! read-ch %) {:prompt (prompt)}))]
    (go
     (loop []
       (let [result (<! eval-result-ch)]
         (when *debug* (.log js/console result))
         (when-let [token (or (aget result "out") (aget result "value"))] (print token))
         (when-let [update-ns (aget result "ns")] (reset! current-ns (symbol update-ns)))
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
      (let [repl-port (<! (resolve-port options))
            nrepl-client (nrepl/connect repl-port)
            session (<! (new-session nrepl-client))
            server-description (<! (async-perform-op nrepl-client {:op "describe" :session session}))]
        (print-intro server-description repl-port)
        (rui)
        (loop [expr (<! eval-ch)]
          (nrepl/perform-op nrepl-client {:op "eval" :code expr :session session} #(put! eval-result-ch %))
          (recur (<! eval-ch)))))))

(defn -main [& args]
  (let [options (-> args args->map)]
    (setup-repl options)))

(set! *main-cli-fn* -main)
