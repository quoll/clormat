(ns ^{:doc "A reimplementation of the format function for ClojureScript"
      :author "Paula Gearon"}
  clormat.core
  (:require [clojure.string :as str])
  #?(:clj
     (:import [java.lang StringBuffer]  ;; this is automatically included
              [java.util FormattableFlags Formattable]
              [java.text NumberFormat])
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
  #?(:clj (Long/toHexString n)
     :cljs (if (pos? n)
             (.toString n 16)
             (let [pn (+ js/Number.MAX_SAFE_INTEGER n 1)
                   s (.toString pn 16)]
               (if (> pn 0x0FFFFFFFFFFFFF)
                 (str "3" (subs s 1))
                 (let [lead (- 14 (count s))]
                   (str (subs "20000000000000" 0 lead) s)))))))

(defn oct
  "Convert an integer to a octal string"
  [n]
  #?(:clj (Long/toOctalString n)
     :cljs (if (pos? n)
             (.toString n 8)
             (if (< n -377777777777777777)
               (throw (ex-info "Number out of range for conversion to octal" {:n n}))
               (let [pn (+ js/Number.MAX_SAFE_INTEGER n 1)
                     s (.toString pn 8)]
                 (if (> pn 077777777777777777)
                   (let [r ({"3" "7" "2" "6" "1" "5"} (first s))]
                     (str r (subs s 1)))
                   (let [lead (- 18 (count s))]
                     (str (subs "40000000000000000" 0 lead) s))))))))

(defn set-width
  "Sets the minimum width of a string, padding with a provided character if needed.
  s: The string to set the minimum width for.
  w: The minimum width, as a character count.
  decr: a decrement to use on the width, if not nil
  l: If true, the the output should be left-justified
  c: The character to pad the width, or space if not provided."
  ([s w decr l] (set-width s w decr l \space))
  ([s w decr l c]
   (or
    (and w (let [wdth (- w decr)
                 size (count s)]
             (and (< size wdth)
                  (let [block (repeat (- wdth size) c)]
                    (if l
                      (apply str s block)
                      (str (apply str block) s))))))
    s)))

(defn err
  [s lexed]
  (let [msg (str "Format conversion error: " s)
        [_ _ _ flags width precision conversion] lexed
        data {:conversion conversion
              :flags flags
              :width width
              :precision precision}]
    (throw (ex-info ~msg ~data))))

(defn format-oct
  "Formats octal numbers according to provided flags and width"
  [a width left flagset?]
  (let [h (oct a)]
    (if (flagset? \#)
      (if (flagset? \0)
        (str "0" (set-width h width 1 left \0))
        (set-width (str "0" h) width 0 left))
      (let [padding (if (flagset? \0) \0 \space)]
        (set-width h width 0 left padding)))))

(defn format-hex
  "Formats hex numbers according to provided flags and width"
  [a width left flagset?]
  (let [h (hex a)]
    (if (flagset? \#)
      (if (flagset? \0)
        (str "0x" (set-width h width 2 left \0))
        (set-width (str "0x" h) width 0 left))
      (let [padding (if (flagset? \0) \0 \space)]
        (set-width h width 0 left padding)))))

(defn dformat
  "Uses the default locale to do number formatting"
  [d group? width]
  #?(:clj (let [nf (NumberFormat/getInstance)]
            (.setGroupingUsed nf (boolean group?))
            (when width (.getMinimumIntegerDigits nf width))
            (.format nf d))
     :cljs (let [options (if group? #js{:useGrouping "true"} #js{})
                 options (if width
                           (js/Object.assign options #js{:minimumSignificantDigits width})
                           options)]
             (.format (js/Intl.NumberFormat) d options))))

(defn format-dec
  "Formats decimal integers according to provided flags and width"
  [a width left flagset?]
  (let [[v sign] (if (neg? a) [(- a) \-] [a \+])
        ;; if the numberformatter is padding with zeros, then determine the width based
        ;; of extra characters that may be added, such as parens or leading +/-
        w (and width
               (flagset? \0)
               (if (= sign \-)
                 (if (flagset? \() (- width 2) (dec width))
                 (if (or (flagset? \+) (flagset? \space)) (dec width))))
        s (dformat v (flagset? \,) w)
        ;; add extra characters to indicate sign
        s (if (= sign \-)
            (if (flagset? \() (str \( s \)) (str \- s))
            (if (flagset? \+)
              (str \+ s)
              (if (flagset? \space)
                (str \space s)
                s)))]
    (set-width s width 0 left)))

(defn convert
  "Converts a lexed specifier and argument into the required string"
  [[_ _ _ flags width precision conversion rem :as lexed] arg]
  (let [width (and width (parse-long width))
        flagset? (reduce (fn [s f] (conj s f)) #{} flags)
        left (flagset? \-)
        precision (and precision (parse-long precision))
        as-string (fn [a] #?(:clj (if (instance? Formattable a)
                                    (let [flag (if left FormattableFlags/LEFT_JUSTIFY 0)]
                                      (.formatTo ^Formattable a flag (or width -1) (or precision -1)))
                                    (set-width (str a) width 0 left))
                             :cljs (set-width (str a) width 0 left)))
        as-char (fn [a] (if (char? a)
                          (let [s (set-width a width 0 left)]
                            (if (= "C" conversion) (str/upper-case s)))
                          (err (str a "is not a character") lexed)))]
    (case conversion
      "b" (set-width (boolean arg) width 0 left)
      "B" (set-width (str/upper-case (boolean arg)) width 0 left)
      "h" (set-width (hex (hash arg)) width 0 left)
      "H" (set-width (str/upper-case (hex (hash arg))) width 0 left)
      "s" (as-string arg)
      "S" (str/upper-case (as-string arg))
      "c" (as-char arg)
      "C" (str/upper-case (as-char arg))
      "d" (cond
            (not (int? arg)) (err (str arg "is not an integer") lexed)
            (flagset? \#) (err "# flag is illegal for integer" lexed)
            :default (format-dec arg width left flagset?))
      "o" (format-oct arg width left flagset?)
      "x" (format-hex arg width left flagset?)
      ;; Note: I would prefer %#X on 15 to return 0xF, not 0XF
      "X" (str/upper-case (format-hex arg width left flagset?))
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
