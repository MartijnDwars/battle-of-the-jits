/*
 * I stumbled upon the following example by accident. C2 runs the following code in ~3000ns, but
 * Graal runs the code in ~50ns. If we look at the generated ASM, we see that C2 generates:
 *
 *          │   0x000000010ee39d78: lock addl $0x0,(%rsp)     ;*synchronization entry
 *          │                                                 ; - java.util.Random::&lt;init&gt;@-1 (line 105)
 *          │                                                 ; - nl.martijndwars.jits.Range::benchmark@4 (line 24)
 *          │                                                 ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   0.29%  │   0x000000010ee39d7d: xor    %r11d,%r11d        ;*synchronization entry
 *          │                                                 ; - java.util.concurrent.atomic.AtomicLong::get@-1 (line 105)
 *          │                                                 ; - java.util.Random::next@8 (line 202)
 *          │                                                 ; - java.util.Random::nextInt@17 (line 390)
 *          │                                                 ; - nl.martijndwars.jits.Range::benchmark@25 (line 28)
 *          │                                                 ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   0.02%  │↗  0x000000010ee39d80: mov    0x10(%rcx),%rax    ;*invokevirtual compareAndSwapLong
 *          ││                                                ; - java.util.concurrent.atomic.AtomicLong::compareAndSet@9 (line 147)
 *          ││                                                ; - java.util.Random::next@32 (line 204)
 *          ││                                                ; - java.util.Random::nextInt@17 (line 390)
 *          ││                                                ; - nl.martijndwars.jits.Range::benchmark@25 (line 28)
 *          ││                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *  28.47%  ││  0x000000010ee39d84: mov    %rax,%r8
 *   0.15%  ││  0x000000010ee39d87: movabs $0x5deece66d,%r10
 *   0.01%  ││  0x000000010ee39d91: imul   %r10,%r8
 *   7.79%  ││  0x000000010ee39d95: add    $0xb,%r8           ;*ladd
 *          ││                                                ; - java.util.Random::next@20 (line 203)
 *          ││                                                ; - java.util.Random::nextInt@17 (line 390)
 *          ││                                                ; - nl.martijndwars.jits.Range::benchmark@25 (line 28)
 *          ││                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   2.98%  ││  0x000000010ee39d99: mov    %r8,%r9
 *   0.04%  ││  0x000000010ee39d9c: movabs $0xffffffffffff,%r10
 *          ││  0x000000010ee39da6: and    %r10,%r9           ;*land
 *          ││                                                ; - java.util.Random::next@24 (line 203)
 *          ││                                                ; - java.util.Random::nextInt@17 (line 390)
 *          ││                                                ; - nl.martijndwars.jits.Range::benchmark@25 (line 28)
 *          ││                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   2.64%  ││  0x000000010ee39da9: lock cmpxchg %r9,0x10(%rcx)
 *  47.57%  ││  0x000000010ee39daf: sete   %r10b
 *
 * whereas Graal generates:
 *
 *   0.01%      ╭  │  0x000000011029ddcb: jmpq   0x000000011029ddd2  ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
 *              │  │                                                ; - nl.martijndwars.jits.Range::benchmark@16 (line 27)
 *              │  │                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   6.23%      │↗ │  0x000000011029ddd0: inc    %esi               ;*iinc {reexecute=0 rethrow=0 return_oop=0}
 *              ││ │                                                ; - nl.martijndwars.jits.Range::benchmark@50 (line 27)
 *              ││ │                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   0.50%      ↘│ │  0x000000011029ddd2: cmp    $0x12c,%esi
 *               ╰ │  0x000000011029ddd8: jl     0x000000011029ddd0  ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
 *                 │                                                ; - nl.martijndwars.jits.Range::benchmark@16 (line 27)
 *                 │                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@17 (line 190)
 *   0.03%         │  0x000000011029ddda: mov    0x10(%rsp),%rsi    ;*invokevirtual consume {reexecute=0 rethrow=0 return_oop=0}
 *                 │                                                ; - nl.martijndwars.jits.generated.Range_benchmark_jmhTest::benchmark_avgt_jmhStub@20 (line 190)
 *   0.63%         │  0x000000011029dddf: mov    $0x0,%edx
 *   0.01%         │  0x000000011029dde4: data16 xchg %ax,%ax
 *                 │  0x000000011029dde7: callq  0x00000001102350a0  ; OopMap{[8]=Oop [16]=Oop [24]=Oop [32]=Oop off=236}
 *
 * As you can see, Graal performs some form of symbolic execution in which it realizes that the if-condition is always
 * false and gets rid of everything except the loop
 * itself.
 */
package nl.martijndwars.jits;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Warmup(iterations = 5, time = 1, timeUnit = SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class Dataflow {
  /**
   * Graal's data flow analysis discovers that the condition `r > 1` can never be true.
   */
  @Benchmark
  public int random() {
    Random random = new Random();
    int sum = 0;
    
    for (int i = 0; i < 256; i++) {
      int r = random.nextInt(2); // Random values are always in [0, 1]
      
      if (r > 1) { // This is always false
        sum += r;
      }
    }
    
    return sum;
  }
  
  @Benchmark // 436ns on Graal, 2594ns on C2. Why is Graal 5x faster?
  public int halfRandom() {
    Random random = new Random();
    int sum = 0;
    
    for (int i = 0; i < 256; i++) {
      Circle circle = new Circle(random.nextInt(2)); // Random values are always in [0, 1]
      
      if (circle.r > 0) { // This is half of the time false
        sum += circle.r;
      }
    }
    
    return sum;
  }
  
  @Benchmark // 3000ns on C2, 500ns on Graal. Why is Graal now slower than before, but not as slow as C2?
  public int power() {
    Random random = new Random();
    int sum = 0;
    
    for (int i = 0; i < 256; i++) {
      Circle circle = new Circle(random.nextInt(2)); // Random values are always in [0, 1]
      
      if (Math.pow(circle.r, 2) > 4) { // This is still still always false
        sum += circle.r;
      }
    }
    
    return sum;
  }
  
  private static class Circle {
    public int r;
    
    public Circle(int r) {
      this.r = r;
    }
  }
}
