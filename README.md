# Thinking and Sleeping Better With Clojure

To prioritize a list of todo-items, as in my Prioritization-app I recently launched, I needed to get a list of the unique combinations for the user to choose from:

It goes a little bit like this:

> Which is more important: **A** or **B**?
> Which is more important: **A** or **C**?
> Which is more important: **B** or **C**?

I wrote a version in Ruby for the app and then a sketch in Clojure to compare readability and expressiveness. 

In my opinion, the Clojure version is much more readable and was far easier to reason about. 

Probably because I'm not good to solve problems in Ruby but there might be more than meets the eye here.

## Creating Comparisons

Let's say I have a todo-list of `A, B, C and D`. 

The list of comparisons I want is `A ↔ B, A ↔ C, A ↔ D, B ↔ C, B ↔ D and C ↔ D` to use later in my app.

From the matrix below, you can see, I want only the _unique_ comparisons (`A ↔ B` and `B ↔ A` are interchangeable for my purposes) and skip dupes that compare an element with itself (`A ↔ A, B ↔ B, C ↔ C, D ↔ D`)

To demonstrate, I only want the top-right triangle of comparisons:

| ×  | A  | B  |  C | D |
|:---:|:---|---|---|---|
| A  | _skip (dupe)_ | **A ↔ B**  | **A ↔ C**  | **A ↔ D** |
| B  | _skip_ | _skip (dupe)_ | **B ↔ C** | **B ↔ D** |
| C  | _skip_ | _skip_ | _skip (dupe)_ | **C ↔ D**  |
| D  | _skip_ | _skip_ | _skip_ |  _skip (dupe)_ |

### Thinking, Testing and Implementing in Ruby

Now, since I started the app in Ruby, I translated the behavior above as follows:

```ruby
describe '.unique_combinations' do
    context 'input list is empty' do
      it 'returns an empty list' do
        result = Combinator.unique_combinations([])
        expect(result).to eq([])
      end
    end

    context 'input list only has one element' do
      it 'returns an empty list' do
        result = Combinator.unique_combinations([1])
        expect(result).to eq([])
      end
    end

    context 'input list only has two elements' do
      it 'returns a list with combination a' do
        result = Combinator.unique_combinations([1, 2])
        expect(result).to eq([[1,2]])
      end

      it 'returns a list with combination b' do
        result = Combinator.unique_combinations([:foo, :bar])
        expect(result).to eq([[:foo, :bar]])
      end
    end

    context 'input list only has three elements' do
      it 'returns a list with 3 combinations' do
        result = Combinator.unique_combinations([1, 2, 3])
        expect(result).to eq([[1, 2], [1, 3], [2, 3]])
      end
    end

    context 'input list only has four elements' do
      it 'returns a list with 6 combinations' do
        result = Combinator.unique_combinations([1, 2, 3, 4])
        expect(result).to eq([[1, 2], [1, 3], [1, 4], [2, 3], [2, 4], [3, 4]])
      end
    end
  End
end
```

...and wrote, after several failed attempts and one sleepless night, the following, working but not very elegant solution. I felt weird about it, because even looking at it now, I can't fully explain it well. It works, it has reasonable test-coverage. But I'm not proud of it. 

```ruby
def self.unique_combinations(ids)
    return [] if ids.length < 2

    all_combinations = []

    for i1 in ids
      for i2 in ids
        if i1 != i2
          contains = false
          for c in all_combinations
            if c[1] == i1 and c[0] == i2
              contains = true
            end
          end
          unless contains
            all_combinations << [i1, i2]
          end
        end
      end
    end

    return all_combinations
  end
```

Okay, fine I thought. How would I solve this issue in Lisp? 

### Thinking, Testing and Implementing in Clojure

I don't know why but thinking in terms of lists and recursion instead of iteration made it simpler for me.

The top-level function works as follows:

```clojure
(final-combinations '(A B C D)) 
;; => '((A B) (A C) (A D) (B C) (B D) (C D))
```

Yup, that's the list I want.
Thinking about it I had an idea to break the problem further down:

The complete list of unique comparisons, the one I want,  is...

1. the **union** of each row of one element combined with all the others. That's `A` combined with `B, C and D`
2. Each row has an increasing **offset** from the left that starts at 0 for row 1 and increases by 1 for each following row.
3. I have to subtract the **dupes** such as `A ↔ A` because I don't need them.

For illustration purposes, here is the table again:

| ×  | A  | B  |  C | D |
|:---:|:---|---|---|---|
| A  | _skip (dupe)_ | **A ↔ B**  | **A ↔ C**  | **A ↔ D** |
| B  | _skip_ | _skip (dupe)_ | **B ↔ C** | **B ↔ D** |
| C  | _skip_ | _skip_ | _skip (dupe)_ | **C ↔ D**  |
| D  | _skip_ | _skip_ | _skip_ |  _skip (dupe)_ |

Given the above 3 steps, I basically just wrote them out as functions.

I derived the first function, `combine` which takes `x` and `l` and returns a list of all possible combinations, including the ones we'll later skip.

```clojure
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
```

Then, I use `combine` to create basically the table above but with nothing yet removed. 

```clojure
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
```

Then I introduce the idea of an offset. To only take those elements in a list that are after a certain offset.

```clojure
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
```

Now I can use `take-rest` and apply it to each row in my matrix...

```clojure
(defn unique-combinations 
  ([offset ll] 
   (unique-combinations offset ll '()))
  ([offset ll acc]
   (cond (empty? ll) (reverse acc)
         :else (recur (inc offset) (rest ll) (cons (take-rest offset (first ll)) acc)))))
```

And finally remove the identity items such as `A ↔ A`

```clojure
(with-test
  (defn final-combinations 
    [l]
    (filter #((= (first %) (second %)))) (partition 2 (flatten (unique-combinations 1 (combine-lists l l)))))

  (is (= (final-combinations '()) '()))
  (is (= (final-combinations '(1)) '()))
  (is (= (final-combinations '(1 2)) '((1 2))))
  (is (= (final-combinations '(1 2 3)) '((1 2) (1 3) (2 3))))
  (is (= (final-combinations '(1 2 3 4)) '((1 2) (1 3) (1 4) (2 3) (2 4) (3 4)))))
```

Granted, this code is not yet refactored and in total more lines than the Ruby version, but here is the rub:

It took me **20 minutes** to think up and implement the solution in Clojure and 2 days including a sleepless night to do it in Ruby. 

I don't know what that means but I'll probably use a Lisp like Clojure for problems like this in the future. Just to make sure I get some sleep.

*

# clojure-scribbles

A few experiments learning Clojure, mostly recursive functions inspired by "The Little Schemer."

## Usage

### Test

All tests are written in-line using the `with-test` macro. Run with `lein test`

## License

Copyright © 2017 Alexis Rondeau

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
