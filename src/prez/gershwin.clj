(ns ^{:doc "Functions for generating prettified Gershwin source for HTML-based presentations."}
  prez.gershwin
  (:require [hiccup.core :refer [html]]
            [clojure.string :as str]))

(defn gwn-markup-word-defs
  "Transform a Gershwin word definition to markup"
  [^String code]
  (-> code
      (str/replace #"(?s)(:\s+)([^\s]+)\s+(\[[^\]]+\])(.*?)( ;)"
                   (html
                    [:span.function
                     [:span.keyword "$1"]
                     [:span.title "$2 "]
                     [:span.prompt "$3"]
                     "$4"
                     [:span.keyword "$5"]]
))))

(defn- replace-words
  "Given a code string, a collection of Gershwin words, and a templated replacement, run clojure.string/replace with each word and replacement against the code."
  [^String code words replacement]
  (:out (reduce (fn [state item]
                  (assoc state :out (str/replace
                                     (:out state)
                                     (re-pattern (if (some #{"<" ">"} words)
                                                   (str "(\\s" item "\\s)")
                                                   (str "\\b(" item ")\\b")))
                                     replacement)))
                {:out code} words)))

(defn gwn-markup
  "Transform Gershwin source code to HTML that can be syntax highlighted."
  [^String code]
  (let [builtins ["swap" "dup" "dup2" "dup3" "rot" "drop" "drop2" "drop3" "nip" "nip2"
                  "over" "over2" "pick" "dip" "dip2" "dip3" "dip4" "dupd" "keep" "keep2"
                  "keep3" "bi" "bi2" "bi3" "tri" "invoke"]
        out (-> code
                gwn-markup-word-defs
                (replace-words builtins (html [:span.keyword "$1"]))
                (replace-words ["<" ">"] (html [:span.quotation "$1"])))]
    out))

;; (replace-words ["<" ">"] (html [:span.quotation "$1"]))

(defn code-gwn
  "HTML code block themed as Gershwin source code"
  [^String code]
  [:pre [:code.python (gwn-markup code)]])