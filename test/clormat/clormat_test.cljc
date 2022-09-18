(ns clormat.clormat-test
  (:require [clojure.test :refer :all]
            [clormat.clormat :refer :all]))

(deftest simple-test
  (testing "Simple substitution"
    (is (= "hello world" (-format "hello %s" "world")))
    (is (= "hello world 5 times") (-format "hello %s %d times" "world" 5))))

(deftest compound-test
  (testing "Combination of argument types"
    (is (= "data 2 1 % 1 2 1 end" (-format "data %2$d %1$d %% %d %d %1$d end" 1 2 3)))))

(deftest hex-test
  (testing "Conversion to hexadecimal"
    (is (= "12d687" (-format "%x" 1234567)))
    (is (= "0x12d687" (-format "%#x" 1234567)))
    (is (= "  12d687" (-format "%8x" 1234567)))
    (is (= "12d687  " (-format "%-8x" 1234567)))
    (is (= "0012d687" (-format "%08x" 1234567)))
    (is (= "12d687" (-format "%2x" 1234567)))
    (is (= "0x12d687" (-format "%#08x" 1234567)))
    (is (= "  0x12d687" (-format "%#10x" 1234567)))
    (is (= "0x0012d687" (-format "%#010x" 1234567)))
    (is (= "0x12d687  " (-format "%#-10x" 1234567)))
    (is (= "12D687" (-format "%X" 1234567)))
    (is (= "0X12D687" (-format "%#X" 1234567)))
    (is (= "  12D687" (-format "%8X" 1234567)))
    (is (= "12D687  " (-format "%-8X" 1234567)))
    (is (= "0012D687" (-format "%08X" 1234567)))
    (is (= "12D687" (-format "%2X" 1234567)))
    (is (= "0X12D687" (-format "%#08X" 1234567)))
    (is (= "  0X12D687" (-format "%#10X" 1234567)))
    (is (= "0X0012D687" (-format "%#010X" 1234567)))
    (is (= "0X12D687  " (-format "%#-10X" 1234567)))
    #?(:cljs
       (is (= "3fffffffffffff" (-format "%x" -1)))
       (is (= "30000000000000" (-format "%x" -0x10000000000000)))
       (is (= "2fffffffffffff" (-format "%x" -0x10000000000001)))
       (is (= "20000000000001" (-format "%x" js/Number.MIN_SAFE_INTEGER))))))

(deftest oct-test
  (testing "Conversion to octal"
    (is (= "4553207" (-format "%o" 1234567)))
    (is (= "04553207" (-format "%#o" 1234567)))
    (is (= "  4553207" (-format "%9o" 1234567)))
    (is (= "4553207  " (-format "%-9o" 1234567)))
    (is (= "004553207" (-format "%09o" 1234567)))
    (is (= "4553207" (-format "%2o" 1234567)))
    (is (= "04553207" (-format "%#08o" 1234567)))
    (is (= "  04553207" (-format "%#11o" 1234567)))
    (is (= "0004553207" (-format "%#011o" 1234567)))
    (is (= "04553207  " (-format "%#-11o" 1234567)))))
