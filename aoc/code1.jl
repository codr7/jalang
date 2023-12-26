(function decode-line [line:String]:Integer
  (parse-integer (string (parse-integer line) (parse-integer (reverse-string line)))))

(function calibrate [input:Path]
  (reduce + (map decode-line (split (slurp input) "\n")) 0))