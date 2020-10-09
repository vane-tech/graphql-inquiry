(ns graphql-inquiry.main
  (:require [clojure.string :as str]))

(defn- throw-error [msg]
  #?(:clj (throw (Error. msg))
     :cljs (throw (js/Error msg))))

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

    :else (throw-error "Invalid query")))

(defn- variable-definition [variable-defs]
  (if (seq variable-defs)
    (str \(
         (->> variable-defs
              (map (fn [[variable type]]
                     (str \$ (name variable) \: (name type))))
              (str/join \,))
         ") ")
    " "))

(defn query [options]
  (cond (sequential? options) (unparse options)
        (map? options) (str "query " (:operation-name options) (variable-definition (:variable-defs options)) (unparse (:query options)))
        :else (throw-error "Invalid query (must be a map or vector)")))

(defn mutation [{:keys [variable-defs query operation-name]}]
  (str "mutation " operation-name (variable-definition variable-defs) (unparse query)))
