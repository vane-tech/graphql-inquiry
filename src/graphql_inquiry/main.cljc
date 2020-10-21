(ns graphql-inquiry.main
  (:require [clojure.string :as str]))

(defn- throw-error [msg]
  #?(:clj (throw (Error. msg))
     :cljs (throw (js/Error msg))))

(declare unparse-args)

(defn- unparse-arg-value [value]
  (cond (keyword? value) (str \$ (name value))
        (or (integer? value)
            (true? value)
            (false? value)) (str value)
        (nil? value) "null"
        (map? value) (str \{ (unparse-args value) \})
        (sequential? value) (str \[ (->> value (map unparse-arg-value) (str/join \,)) \])
        :else (str \" value \")))

(defn- unparse-args [args]
  (str/join
   \,
   (map (fn [[key value]]
          (str (name key) ":" (unparse-arg-value value)))
        args)))

(defn- unparse [query-structure]
  (cond
    (keyword? query-structure) (name query-structure)

    (map? query-structure) (str \( (unparse-args query-structure) \))

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
