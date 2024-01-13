## Introduction
This project aims to implement an embedded custom Lisp interpreter in Java.

## Setup
The current binary may be found in the project root, launching it without arguments starts a REPL. `rlwrap` may be used to add support for line editing.

```
$ git clone https://github.com/codr7/jalang.git
$ cd jalang
$ rlwrap java -jar jalang.jar

jalang v7
May the source be with you!

  (say "hello world")

hello world
_
```

## Flow Control

### or
`or` evaluates its arguments in specified order until the result is true.

```
  (or F 0 "" [] {} 'foo 'bar)

'foo
```

### and
`and` evaluates its arguments in specified order as long as the result is true.

```
  (and T 42 "foo" [1] {1:2} 'foo 'bar)

'bar
```

### if
`if` allows branching on a condition.<br/>
The else expression is optional.

```
  (if T "true")

"true"
```
```
  (if F "true" "false")

"false"
```

### do
`do` may be used to pass multiple expressions where one is expected.

```
  (if T (do
    (say "first")
    (say "second")))

first
second
T
```

### function
`function` may be used to create new functions.

```
  (function add-42 [x]
    (+ x 42))

  (add-42 1)

43
```

Omitting the name creates an anonymous function.

```
  (function [x]
    (+ x 42))

  (Function [x])
```

## Bindings
`define` may be used to introduce compile time bindings.

```
  (define foo (+ 35 7))
  foo

42 
```

`let` may be used to introduce scoped runtime bindings.

```
  (let [foo 35
        bar (+ foo 7)]
    bar)

42
```

Defined names are dynamically scoped.

```
  (define foo _)

  (function bar []
    foo)

  (function baz []
    (let [foo 42]
      (bar)))

  (baz)

42
```

## Aggregates

### Pairs
Pairs may be created using `:`.

```
  1:2

1:2
```

`head` and `tail` may be used to extract values from a pair.

```
  (tail 1:2)

2
```

### Lists
A list is a pair with another pair in tail position.

```
  (tail 1:2:3)

2:3
```

Besides the literal syntax, lists may be created using the `List` constructor.

```
  (List 1 2 3)

1:2:3
```

Lists support stack semantics.

```
  (push 2:3 1)

1:2:3
```
```
  (peek 1:2:3)

1
```
```
  (pop 1:2:3)

1
```

### Vectors
New vectors may be created using `[...]`,


```
  [1 2 3]

[1 2 3]
```

or by calling the `Vector` constructor.

```
  (Vector 1 2 3)

[1 2 3]
```

Vectors support stack semantics.

```
  (push [1 2] 3)

[1 2 3]
```
```
  (peek [1 2 3])

3
```
```
  (pop [1 2 3])

3
```

Calling a vector returns the value at the specified index (`_` if the index is out of bounds), or updates it depending on the number of arguments.

```
  (['foo 'bar 'baz] 1)

'bar
```
```
  (['foo 'bar 'baz] 1 'qux)

['foo 'qux 'baz]
```

### Maps
Ordered maps may be created using `{...}`,

```
  {3:4 1:2}

{1:2 3:4}
```

or by calling the `Map` constructor with alternating keys and values.

```
  (Map 1 2 3 4)

{1:2 3:4}
```

Calling a map returns the value for the specified key (`_` if not found) or updates it depending on the number of arguments.

```
  ({'foo:1 'bar:2 'baz:3} 'bar)

2
```
```
  ({'foo:1 'bar:2 'baz:3} 'bar 4)

{'foo:1 'bar:4 'baz:3}
```

### Sets
Ordered sets may be created using `{...}`,

```
  {4 3 2 1}

{1 2 3 4}
```

or by calling the `Set` constructor.

```
  (Set 4 3 2 1)

{1 2 3 4}
```

## Iterators
`map` may be used to map a function over any number of iterable values, it returns an iterator that may be further processed or drained into an aggregate.

```
  (Vector (map + [1 2 3] [4 5 6 7]))

[5 7 9]
```

`reduce` may be used to reduce any iterable value by passing items left to right and the current result to the specified function.

```
  (reduce + [2 3] 1)

6
```

`append` may be used to join multiple iterables.

```
  (Vector (append [1 2 3] {'foo 'bar 'baz} "abc"))

[1 2 3 'bar 'baz 'foo \a \b \c]
```

`interleave` may be used to braid multiple iterables into a single sequence.

```
  (Vector (interleave 1:2 {'foo 'bar 'baz} "abc"))

[1 'bar \a 2 'baz \b 'foo \c]
```

`zip` may be used to create pairs/lists from an arbitrary number of iterables.

```
  (Vector (zip 1:2:3 "abc" 'foo:'bar:'baz))

[1:\a:'foo 2:\b:'bar 3:\c:'baz]
```

`unzip` may be used to break up pairs/lists.

```
  (Vector (map Vector (unzip [1:\a:'foo 2:\b:'bar 3:\c:'baz])))

[[1 2 3] [\a \b \c] ['foo 'bar 'baz]]
```

## Loops
`for` evaluates its body with the specified variable bound to successive values of its input.

```
(for [v ['foo 'bar 'baz]]
  (say v))

foo
bar
baz
```

## Tests
[tests.jlg](https://github.com/codr7/jalang/blob/main/tests.jlg) contains the humble beginnings of a test suite.

### check
`check` may be used to implement tests, it signals an error if the result of its body differs from the first argument.

```
  (check T F)

EvaluationError in repl@1:1: Test failed; expected: T, actual: F.
```

## Benchmarks

The core interpreter is currently roughly as fast as Python, but there is plenty of low hanging fruit left to pick.

```
$ java -jar jalang.jar benchmarks.jlg
PT0.14015925S
PT0.152264167S
PT0.058910375S
```
```
$ python3 python/fib.py
0.12464387500000002
0.12554220899999996
0.0671775
```

### benchmark
`(benchmark n ...)` measures the time it takes to repeat its body `n` times after warming up the JIT.

```
  (benchmark 10 (sleep (milliseconds 100)))

PT1.020851167S
```