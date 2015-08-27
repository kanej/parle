parle
=====

A clojure nREPL command line client written in clojurescript running on node.js.

Install
-------
```bash
> npm install -g parle
```

Usage
-----

Start an nrepl server with leiningen

```bash
> lein repl :start :port 10888
```

In a separate terminal

```bash
> parle --port 10888
```

Optionally, parle can pick up the .nrepl-port file from the current directory, so if the parle command
is run from the same directory as the lein repl is started it will pick it up automatically.

