(check T (= 42 42))
(check F (= 42 41))

(check 5 (+ 3 2))
(check 1 (- 3 2))

(check 5 (+1 4))
(check 4 (-1 5))

(check 1 (head 1:2))
(check 2 (tail 1:2))

(check 1 (head 1:2:3))
(check 2:3 (tail 1:2:3))

(check ["foo" "bar" "baz"] (split "foo bar baz" " "))

(check "1 2 3" (string 1 " " 2 " " 3))

(check "oof" (reverse-string "foo"))

(check 42:3 (parse-integer " 42foo"))

(check 3 (length "foo"))
(check 3 (length [1 2 3]))

(check [0 1 2] (deque 3))
(check [5 7 9] (deque (map + [1 2 3] [4 5 6 7])))