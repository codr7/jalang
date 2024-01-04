## Introduction
This project aims to implement an embedded custom Lisp interpreter in Java.

## Setup
The current binary may be found in the project root, launching it without arguments starts a REPL.

```
$ git clone https://github.com/codr7/jalang.git
$ cd jalang
$ java -jar jalang.jar

jalang v1
May the source be with you!

  (say "hello world")

hello world
_
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

### Deques
The main indexed collection is the double ended queue.<br/>
Deques may be crated using `[...]`.


```
  (length [1 2 3:4])

3
```

## Performance
The core interpreter is currently roughly as fast as Python, but there is plenty of low hanging fruit left to pick.