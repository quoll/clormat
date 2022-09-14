(ns ^{:doc "A reimplementation of the format function for ClojureScript"
      :author "Paula Gearon"}
  format.core
  (:require [clojure.string :as str])
  #?(:clj
     (:import [java.lang StringBuffer]  ;; this is automatically included
              [java.util Formatter FormattableFlags])
     :cljs
     (:import [goog.string StringBuffer])))

(defn new-buffer
  "non-interop function for creating a string buffer"
  ([] (StringBuffer.))
  ([init] (StringBuffer. init)))

(defn hex
  "Convert an integer to a hexadecimal string"
  [n]
  #?(:clj (Long/toHexString n) :cljs (.toString n 16)))

(defn set-width
  "Sets the minimum width of a string, padding with a provided character if needed.
  s: The string to set the minimum width for.
  w: The minimum width, as a character count.
  c: The character to pad the width, or space if not provided."
  ([s w] (set-width s w \space))
  ([s w c]
   (or
    (and w (let [size (count s)]
             (and (< size w) (apply str s (repeat (- w size) c)))))
    s)))

(defmacro err
  [s lexed]
  (let [msg (str "Format conversion error: " s)
        [_ _ _ flags width precision conversion] lexed
        data {:conversion conversion
              :flags flags
              :width width
              :precision precision}]
    `(throw (ex-info ~msg ~data))))

(defn convert
  "Converts a lexed specifier and argument into the required string"
  [[_ _ _ flags width precision conversion rem :as lexed] arg]
  (let [width (and width (parse-long width))
        precision (and precision (parse-long precision))]
    (case conversion
      ("b" "B") (str (boolean arg))
      "h" (set-width (hex arg) width)
      "H" (set-width (str/upper-case (hex arg)) width)
      ("s" "S") #?(:clj (if (implements? arg Formattable)
                          (let [flag (if (str/index-of flags \-) FormattableFlags.LEFT_JUSTIFY 0)]
                            (.formatTo ^Formattable arg flag (or width -1) (or precision -1)))
                          (str arg))
                   :cljs (str arg))
      ("c" "C") (if (char? arg)
                  (str arg)
                  (err (str arg "is not a character") lexed))
      "d" arg
      "o" arg
      "x" arg
      "X" arg
      "e" arg
      "E" arg
      "f" arg
      "g" arg
      "G" arg
      "a" arg
      "A" arg
      ("t" "T") arg  ;; picks up the date/time description from the remaining
      "n" arg
      "%" "%"
      (err (str "Unknown conversion: " conversion) lexed))))

(def spec "([^%]*)%(\\d+\\$)?([-#+ 0,(]*)?(\\d*)(\\.\\d+)?([bBhHsScCdoxXeEfgGaAtTn%])")

(def start-re (re-pattern spec))

(def gen-re (re-pattern (str spec "(.*)")))

(defn format*
  [s & args]
  (let [[_ start] (re-find start-re s)
        tail (subs s (count start))]
    (loop [[arg & rargs :as current-args] args
           result (new-buffer start)
           [[_ lead arg-idx _ _ _ conversion rem :as lexed]] (re-seq gen-re tail)
           tested tail]
      (let [result (if lead (.append result lead) result)]
        (if lexed
          (if arg-idx
            (let [idx (dec (parse-long (subs arg-idx 0 (dec (count arg-idx)))))]
              (recur current-args
                     (.append result (convert lexed (nth args idx)))
                     (re-seq gen-re rem)
                     rem))
            (recur (if (= "%" conversion) current-args rargs)
                   (.append result (convert lexed arg))
                   (re-seq gen-re rem)
                   rem))
          (str (.append result tested)))))))
