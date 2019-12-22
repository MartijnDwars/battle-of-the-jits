# Battle of the JITs

## Build & run

```bash
$ ./run.sh
```

## Results

```
Graal

Benchmark      (mode)  Mode  Cnt    Score    Error  Units
Inlining.test    mono  avgt   10  202.350 ±  2.967  ns/op
Inlining.test    mega  avgt   10  423.298 ± 14.133  ns/op

C2

Benchmark      (mode)  Mode  Cnt    Score    Error  Units
Inlining.test    mono  avgt   10  238.480 ±  0.671  ns/op
Inlining.test    mega  avgt   10  633.847 ± 15.546  ns/op
```

## Todo

* Inlining benchmark for 3, 4, 5, 6, ... to see how it grows.
* Escape analysis benchmark? With GC stats?
  * Scalar Replacement
  * Stack Allocation
  * Lock Elision

## Resources

* Ionut Balosin gave an excellent talk which inspired me to re-create his benchmarks on GraalVM EE. See https://ionutbalosin.com/2019/04/jvm-jit-compilers-benchmarks-report-19-04/.
* Aleksey Shipilёv is the authority on JVM performance tuning. He does a great job at explaining virtual calls in https://shipilev.net/jvm/anatomy-quarks/16-megamorphic-virtual-calls/.