## Introduction
This project aims to implement an embedded custom Lisp interpreter in Java.

## Setup
The current binary may be found in the project root, launching it without arguments starts a REPL. `rlwrap` may be used to add support for line editing.

```
$ git clone https://github.com/codr7/jalang.git
$ cd jalang
$ rlwrap java -jar jalang.jar

jalang v5
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
A list is simply a pair with another pair in tail position.

```
  (tail 1:2:3)

2:3
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
New vectors may be created using `[...]`.


```
  (length [1 2 3:4])

3
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
Maps may be created using `{...}`.

```
  (length {1 2 3:4})

3
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