(function decode-line [line:String]:Int
  (Int/parse (string (Int/parse line) (Int/parse (String/reverse line)))))

(function calibrate [input:Path]
  (reduce + (map decode-line (split (slurp input) "\n")) 0))