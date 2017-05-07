(ns clojure-scribbles.core
  (:require [clojure.test :refer :all]))

(with-test 
  (defn combine 
    ([x l] 
     (combine x l '()))
    ([x l acc]
     (cond (empty? l) (reverse acc)
           :else (recur x (rest l) (conj acc (list x (first l)))) )))
  
  (is (= (combine 1 '()) '()))
  (is (= (combine 1 '(1)) '((1 1))))
  (is (= (combine 1 '(2)) '((1 2))))
  (is (= (combine 1 '(1 2)) '((1 1) (1 2))))
  (is (= (combine 1 '(1 2 3)) '((1 1) (1 2) (1 3)))))

(with-test
  (defn combine-lists 
    ([l1 l2] 
     (combine-lists l1 l2 '()))
    ([l1 l2 acc]
     (cond (empty? l1) (reverse acc)
           :else (recur (rest l1) l2 (cons (combine (first l1) l2) acc)) )))
  
  (is (= (combine-lists '() '()) '()))
  (is (= (combine-lists '(1) '(1)) '(((1 1)))))
  (is (= (combine-lists '(1 2) '(1 2)) '(((1 1) (1 2)) 
                                         ((2 1) (2 2))))))

(with-test
  (defn take-rest
    ([offset l]
     (cond (= offset 0) l
           :else (recur (dec offset) (rest l)))))
  
  (is (= (take-rest 0 '()) '()))
  (is (= (take-rest 1 '()) '()))
  (is (= (take-rest 0 '(1)) '(1)))
  (is (= (take-rest 0 '(1 2)) '(1 2)))
  (is (= (take-rest 1 '(1 2)) '(2)))
  (is (= (take-rest 1 '(1 2 3)) '(2 3)))
  (is (= (take-rest 2 '(1 2 3)) '(3)))
  (is (= (take-rest 2 '(1 2 3 4)) '(3 4))))

(defn unique-combinations 
  ([offset ll] 
   (unique-combinations offset ll '()))
  ([offset ll acc]
   (cond (empty? ll) (reverse acc)
         :else (recur (inc offset) (rest ll) (cons (take-rest offset (first ll)) acc)))))

(with-test
  (defn final-combinations 
    [l]
    (filter #((= (first %) (second %)))) (partition 2 (flatten (unique-combinations 1 (combine-lists l l)))))

  (is (= (final-combinations '()) '()))
  (is (= (final-combinations '(1)) '()))
  (is (= (final-combinations '(1 2)) '((1 2))))
  (is (= (final-combinations '(1 2 3)) '((1 2) (1 3) (2 3))))
  (is (= (final-combinations '(1 2 3 4)) '((1 2) (1 3) (1 4) (2 3) (2 4) (3 4)))))

(with-test
  (defn rember
    ([a lat] (rember a lat '()))

    ([a lat acc]
     (cond (empty? lat) acc
           (= (first lat) a) (concat acc (rest lat))
           :else (recur a 
                        (rest lat) 
                        (conj acc (first lat))))))


  (is (= (rember 'a '()) '()))
  (is (= (rember 'a '(b)) '(b)))
  (is (= (rember 'a '(a)) '()))
  (is (= (rember 'a '(b a)) '(b))))
