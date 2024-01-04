## Introduction
This project aims to implement an embedded custom Lisp interpreter in Java.

## Pairs
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

## Lists
A list is a pair with another pair in tail position.

```
  (tail 1:2:3)

2:3
```

## Performance
The core interpreter is currently roughly as fast as Python, but there is plenty of low hanging fruit left to pick.