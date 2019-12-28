# Battle of the JITs

Inspired by Ionut Balosin's _A Race Of Two Compilers_ and many of Aleksey Shipilev's amazing blog post. Another set of benchmarks with numbers on GraalVM EE.

## Experiments

### Inlining

Dynamic languages
Concrete implementation needs to be determined at runtime
E.g. `x + y` is different for `Number`s and `String`s in JavaScript.

This problem also occurs in static languages like Java.
Polymorphism.

* A _monomorphic_ call site is a call site that (???) 
* Bimorphic call site ???
* Megamorphic call site ???

### Escape Analysis

Escape analysis, (???definition???)

Escape analysis enables three optimisations: _Stack Allocation_, _Scalar Replacement_, and _Lock Elision_.
With Stack Allocation, an object is allocated on the stack instead of the heap.v (???definition???)
Scalar Replacement replaces an object by its fields, which also avoids an expensive allocation on the heap.
Finally, Lock Elision gets rid of the lock operation on methods that are safe to execute concurrently.

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

Benchmark                                     Mode  Cnt    Score    Error   Units
ScalarReplacement.single                      avgt   15  639.570 ± 16.866   ns/op
ScalarReplacement.single:·gc.alloc.rate       avgt   15   ≈ 10⁻⁴           MB/sec
ScalarReplacement.single:·gc.alloc.rate.norm  avgt   15   ≈ 10⁻⁴             B/op
ScalarReplacement.single:·gc.count            avgt   15      ≈ 0           counts
ScalarReplacement.split                       avgt   15  628.544 ±  5.133   ns/op
ScalarReplacement.split:·gc.alloc.rate        avgt   15   ≈ 10⁻⁴           MB/sec
ScalarReplacement.split:·gc.alloc.rate.norm   avgt   15   ≈ 10⁻⁴             B/op
ScalarReplacement.split:·gc.count             avgt   15      ≈ 0           counts

Benchmark                  Mode  Cnt    Score    Error  Units
Escape.branchEscape        avgt   10  194.553 ± 22.953  ns/op
Escape.noEscape            avgt   10  191.757 ± 18.124  ns/op
Escape.noEscapeWithRandom  avgt   10   58.931 ± 28.296  ns/op

C2

Benchmark      (mode)  Mode  Cnt    Score    Error  Units
Inlining.test    mono  avgt   10  238.480 ±  0.671  ns/op
Inlining.test    mega  avgt   10  633.847 ± 15.546  ns/op

Benchmark                                                 Mode  Cnt     Score     Error   Units
ScalarReplacement.single                                  avgt   15   538.866 ±  15.016   ns/op
ScalarReplacement.single:·gc.alloc.rate                   avgt   15    ≈ 10⁻⁴            MB/sec
ScalarReplacement.single:·gc.alloc.rate.norm              avgt   15    ≈ 10⁻⁴              B/op
ScalarReplacement.single:·gc.count                        avgt   15       ≈ 0            counts
ScalarReplacement.split                                   avgt   15  1149.749 ±  65.474   ns/op
ScalarReplacement.split:·gc.alloc.rate                    avgt   15  2656.058 ± 135.539  MB/sec
ScalarReplacement.split:·gc.alloc.rate.norm               avgt   15  4800.000 ±   0.001    B/op
ScalarReplacement.split:·gc.churn.PS_Eden_Space           avgt   15  2720.297 ± 212.830  MB/sec
ScalarReplacement.split:·gc.churn.PS_Eden_Space.norm      avgt   15  4916.890 ± 312.938    B/op
ScalarReplacement.split:·gc.churn.PS_Survivor_Space       avgt   15     0.066 ±   0.029  MB/sec
ScalarReplacement.split:·gc.churn.PS_Survivor_Space.norm  avgt   15     0.119 ±   0.051    B/op
ScalarReplacement.split:·gc.count                         avgt   15   149.000            counts
ScalarReplacement.split:·gc.time                          avgt   15   103.000                ms

Benchmark                  Mode  Cnt    Score    Error  Units
Escape.branchEscape        avgt   10   185.242 ±  30.002  ns/op
Escape.noEscape            avgt   10   168.426 ±   5.437  ns/op
Escape.noEscapeWithRandom  avgt   10  2959.372 ± 209.730  ns/op
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
* In another great article, Aleksey Shipilёv explains scalar replacement: https://shipilev.net/jvm/anatomy-quarks/18-scalar-replacement/.
