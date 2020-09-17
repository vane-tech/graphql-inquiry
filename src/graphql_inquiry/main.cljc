(ns graphql-inquiry.main
  (:require [clojure.string :as str]))

(defn- throw-error []
  #?(:clj (throw (Error. "Cannot parse query"))
     :cljs (js/Error "Cannot parse query")))

(defn- unparse [query-structure]
  (cond
    (keyword? query-structure) (name query-structure)

    (map? query-structure) (str \(
                                (str/join
                                  \,
                                  (map (fn [[k v]]
                                         (let [v (cond (keyword? v) (str \$ (name v))
                                                       (or (integer? v)
                                                           (true? v)
                                                           (false? v)) (str v)
                                                       (nil? v) "null"
                                                       :else (str \" v \"))]
                                           (str (name k) ":" v)))
                                       query-structure))
                                \))

    (sequential? query-structure) (str \{
                                       (str/join
                                         \space
                                         (map unparse query-structure))
                                       \})

    :else (throw-error)))

(defn- type-definition [variables]
  (when (seq variables)
    (str \(
         (->> variables
              (map (fn [[variable type]]
                     (str \$ (name variable) \: (name type))))
              (str/join \,))
         ") ")))

(defn query [options]
  (cond (sequential? options) (unparse options)
        (map? options) (str "query " (type-definition (:variables options)) (unparse (:query options)))
        :else (throw-error)))

(defn mutation [{:keys [variables query]}]
  (str "mutation " (type-definition variables) (unparse query)))
