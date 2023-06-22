(ns build
  (:require [clojure.tools.build.api :as b]
            [shadow.cljs.devtools.api :as shadow]))

(def lib 'ikhaldeev/hs)
(def version (format "0.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (shadow/release :app)
  (b/copy-dir {:src-dirs ["src/clj" "src/cljc" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src/clj" "src/cljc"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'hs.core}))
