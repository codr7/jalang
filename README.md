## Introduction
This project aims to implement an embedded custom Lisp interpreter in Java.

## Setup
The current binary may be found in the project root, launching it without arguments starts a REPL. `rlwrap` may be used to add support for line editing.

```
$ git clone https://github.com/codr7/jalang.git
$ cd jalang
$ rlwrap java -jar jalang.jar

jalang v2
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

### functions
`function` may be used to create new functions.

```
  (function add-42 [x:Integer]:Integer
    (+ x 42))

  (add-42 1)

43
```

Types are optional.

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

  (Function [x:Any])
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

### Maps
Maps may be created using `{...}`.

```
  (length {1 2 3:4})

3
```

## Performance
The core interpreter is currently roughly as fast as Python, but there is plenty of low hanging fruit left to pick.