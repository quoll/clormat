(ns clormat.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clormat.core :refer [-format]]))

#_(deftest simple-test
  (testing "Simple substitution"
    (is (= "hello world" (-format "hello %s" "world")))
    (is (= "hello world 5 times") (-format "hello %s %d times" "world" 5))))

#_(deftest compound-test
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
       (do
         (is (= "3fffffffffffff" (-format "%x" -1)))
         (is (= "20000000000001" (-format "%x" js/Number.MIN_SAFE_INTEGER)))
         (is (= "30000000000000" (-format "%x" -0x10000000000000)))
         (is (= "2fffffffffffff" (-format "%x" -0x10000000000001)))))))

(deftest oct-test
  (testing "Conversion to octal"
    (is (= "4553207" (-format "%o" 1234567)))
    (is (= "04553207" (-format "%#o" 1234567)))
    (is (= "  4553207" (-format "%9o" 1234567)))
    (is (= "4553207  " (-format "%-9o" 1234567)))
    (is (= "004553207" (-format "%09o" 1234567)))
    (is (= "4553207" (-format "%2o" 1234567)))
    (is (= "04553207" (-format "%#08o" 1234567)))
    (is (= "   04553207" (-format "%#11o" 1234567)))
    (is (= "00004553207" (-format "%#011o" 1234567)))
    (is (= "04553207   " (-format "%#-11o" 1234567)))
    #?(:cljs
       (do
         (is (= "777777777777777777" (-format "%o" -1)))
         (is (= "700000000000000000" (-format "%o" -0100000000000000000)))
         (is (= "677777777777777777" (-format "%o" -0100000000000000001)))
         (is (= "400000000000000001" (-format "%o" js/Number.MIN_SAFE_INTEGER)))
         (is (= "500000000000000000" (-format "%o" -0300000000000000000)))
         (is (= "400000000000000001" (-format "%o" -0377777777777777777)))))))

(deftest hash-test
  (testing "Hash output"
    (is (= "6645faa3" (-format "%h" "hello")))
    (is (= "6645FAA3" (-format "%H" "hello")))
    (is (= "  6645faa3" (-format "%10h" "hello")))
    (is (= "6645faa3  " (-format "%-10h" "hello")))))

(deftest bool-test
  (testing "boolean output"
    (is (= "true" (-format "%b" true)))
    (is (= "false" (-format "%b" false)))
    (is (= "TRUE" (-format "%B" true)))
    (is (= "true" (-format "%b" 1)))
    (is (= "false" (-format "%b" nil)))
    (is (= "      true" (-format "%10b" "hello")))
    (is (= "true      " (-format "%-10b" true)))))

(deftest int-test
  (testing "integer output"
    (is (= "101" (-format "%d" 101)))
    (is (= "-101" (-format "%d" -101)))
    (is (= "+101" (-format "%+d" 101)))
    (is (= "-101" (-format "%+d" -101)))
    (is (= " 101" (-format "% d" 101)))
    (is (= "-101" (-format "% d" -101)))
    (is (= "101" (-format "%(d" 101)))
    (is (= "(101)" (-format "%(d" -101)))
    ;; This is locale specific!
    (is (= "1,234,567" (-format "%,d" 1234567)))
    (is (= "-1,234,567" (-format "%,d" -1234567)))
    (is (= "+1,234,567" (-format "%+,d" 1234567)))
    (is (= "-1,234,567" (-format "%+,d" -1234567)))
    (is (= " 1,234,567" (-format "% ,d" 1234567)))
    (is (= "-1,234,567" (-format "% ,d" -1234567)))
    (is (= "1,234,567" (-format "%(,d" 1234567)))
    (is (= "(1,234,567)" (-format "%(,d" -1234567)))

    (is (= "       101" (-format "%10d" 101)))
    (is (= "      -101" (-format "%10d" -101)))
    (is (= "0000000101" (-format "%010d" 101)))
    (is (= "-000000101" (-format "%010d" -101)))
    (is (= "101       " (-format "%-10d" 101)))
    (is (= "-101      " (-format "%-10d" -101)))
    (is (= "      +101" (-format "%+10d" 101)))
    (is (= "      -101" (-format "%+10d" -101)))
    (is (= "+000000101" (-format "%+010d" 101)))
    (is (= "-000000101" (-format "%+010d" -101)))
    (is (= "       101" (-format "%(10d" 101)))
    (is (= "     (101)" (-format "%(10d" -101)))
    (is (= "0000000101" (-format "%0(10d" 101)))
    (is (= "(00000101)" (-format "%0(10d" -101)))
    (is (= "0000000101" (-format "%0,(10d" 101)))
    (is (= "(00000101)" (-format "%0,(10d" -101)))
    (is (= "(0001,234)" (-format "%0,(10d" -1234)))
    ;; This is locale specific!
    (is (= "   1,234,567" (-format "%,12d" 1234567)))
    (is (= "  -1,234,567" (-format "%,12d" -1234567)))
    (is (= "  +1,234,567" (-format "%+,12d" 1234567)))
    (is (= "  -1,234,567" (-format "%+,12d" -1234567)))
    (is (= "   1,234,567" (-format "% ,12d" 1234567)))
    (is (= "  -1,234,567" (-format "% ,12d" -1234567)))
    (is (= "   1,234,567" (-format "%(,12d" 1234567)))
    (is (= " (1,234,567)" (-format "%(,12d" -1234567)))))

#?(:cljs (cljs.test/run-tests))
