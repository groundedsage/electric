(ns build
  "build electric.jar library artifact and demos"
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]
            [shadow.cljs.devtools.api :as shadow-api]
            [shadow.cljs.devtools.server :as shadow-server]
            ))

(def lib 'com.hyperfiddle/electric)
(def version "0.0.0-alpha0")
(def basis (b/create-basis {:project "deps.edn"}))

;;; Library

(defn compile-java [_]
  (b/javac {:src-dirs ["src"]
            :class-dir "src"
            :basis basis
            :javac-opts ["-source" "8" "-target" "8"]}))

(def defaults {:src-pom "pom.xml" :lib lib})

(defn clean [opts]
  (bb/clean opts))

(defn jar [opts]
  (bb/jar (merge defaults opts)))

(defn install [opts]
  (bb/install (merge defaults opts)))

(defn deploy [opts]
  (bb/deploy (merge defaults opts)))

;;; Uberjar

(def class-dir "target/classes")
(def default-jar-name (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean-cljs [_]
  (b/delete {:path "resources/public/js"}))

(defn build-client [{:keys [optimize debug verbose] :or {optimize true, debug false, verbose false}}]
  (shadow-server/start!)
  (shadow-api/release :prod {:debug debug,
                             :verbose verbose,
                             :config-merge (when-not optimize
                                             [{:compiler-options {:optimizations :simple}}])})
  (shadow-server/stop!))

(defn uberjar [{:keys [jar-name optimize debug verbose]
                :or   {jar-name default-jar-name, optimize true, debug false, verbose false}}]
  (println "Cleaning up before build")
  (clean nil)

  (println "Cleaning cljs compiler output")
  (clean-cljs nil)

  (println "Building client")
  (build-client {:optimize optimize, :debug debug, :verbose verbose})

  (println "Bundling sources")
  (b/copy-dir {:src-dirs   ["src" "src-dev" "src-docs" "resources"]
               :target-dir class-dir})

  (println "Compiling server entrypoint")
  (b/compile-clj {:basis      basis
                  :src-dirs   ["src" "src-dev" "src-docs"]
                  :ns-compile '[user]
                  :class-dir  class-dir})

  (println "Building uberjar")
  (b/uber {:class-dir class-dir
           :uber-file (str jar-name)
           :basis     basis
           :main      'user}))


(defn noop [_])                         ; run to preload mvn deps
