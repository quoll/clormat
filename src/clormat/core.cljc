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

(defn append
  "Adds data to the end of a buffer. Abstracted so a different approach can also be used."
  [b d]
  (.append b d))

(defn hex
  "Convert an integer to a hexadecimal string"
  [n]
  #?(:clj (Long/toHexString n) :cljs (.toString n 16)))

(defn oct
  "Convert an integer to a octal string"
  [n]
  #?(:clj (Long/toOctalString n) :cljs (.toString n 8)))

(defn set-width
  "Sets the minimum width of a string, padding with a provided character if needed.
  s: The string to set the minimum width for.
  w: The minimum width, as a character count.
  l: If true, the the output should be left-justified
  c: The character to pad the width, or space if not provided."
  ([s w l] (set-width s w l \space))
  ([s w l c]
   (or
    (and w (let [size (count s)]
             (and (< size w)
                  (let [block (repeat (- w size) c)]
                    (if left
                      (str (apply str block) s)
                      (apply str s block))))))
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
        flagset? (reduce (fn [s f] (conj s f)) #{} flags)
        left (flagset? \-)
        precision (and precision (parse-long precision))
        as-string (fn [a] #?(:clj (if (implements? a Formattable)
                                    (let [flag (if left FormattableFlags.LEFT_JUSTIFY 0)]
                                      (.formatTo ^Formattable arg flag (or width -1) (or precision -1)))
                                    (set-width (str a) width left))
                             :cljs (set-width (str a) width left)))
        as-char (fn [a] (if (char? a)
                          (let [s (set-width a width left)]
                            (if (= "C" conversion) (str/upper-case s)))
                          (err (str a "is not a character") lexed)))]
    (case conversion
      "b" (set-width (boolean arg) width left)
      "B" (set-width (str/upper-case (boolean arg)) width left)
      "h" (set-width (hex (hash arg)) width)
      "H" (set-width (str/upper-case (hex (hash arg))) width)
      "s" (as-string arg)
      "S" (str/upper-case (as-string arg))
      "c" (as-char arg)
      "C" (str/upper-case (as-char arg))
      "d" (if (int? arg)
            (let [padding (if (flagset? \0) \0 \space)]
              (set-width arg width left padding))
            (err (str arg "is not an integer") lexed))
      "o" (set-width (oct arg) width)
      "x" (set-width (hex arg) width)
      "X" (set-width (str/upper-case (hex arg)) width)
      "e" arg
      "E" arg
      "f" arg
      "g" arg
      "G" arg
      "a" arg
      "A" arg
      ("t" "T") arg ;; picks up the date/time description from the remaining
      "n" arg
      "%" "%"
      (err (str "Unknown conversion: " conversion) lexed))))

(def spec #"([^%]*)%(\d+\$)?([-#+ 0,(]*)?(\d*)(\.\d+)?([bBhHsScCdoxXeEfgGaAtTn%])(.*)")

(defn -format
  [s & args]
  (let [start-pos (str/index-of s \%)
        start (subs s 0 start-pos)
        tail (subs s start-pos)]
    (loop [[arg & rargs :as current-args] args
           result (new-buffer start)
           [[_ lead arg-idx _ _ _ conversion rem :as lexed]] (re-seq spec tail)
           tested tail]
      (let [result (if lead (.append result lead) result)]
        (if lexed
          (if arg-idx
            (let [idx (dec (parse-long (subs arg-idx 0 (dec (count arg-idx)))))]
              (recur current-args
                     (.append result (convert lexed (nth args idx)))
                     (re-seq spec rem)
                     rem))
            (recur (if (= "%" conversion) current-args rargs)
                   (.append result (convert lexed arg))
                   (re-seq spec rem)
                   rem))
          (str (.append result tested)))))))

#?(:cljs (def format -format))