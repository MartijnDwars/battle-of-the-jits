/*
 * See https://shipilev.net/jvm/anatomy-quarks/16-megamorphic-virtual-calls/
 *
 * This benchmark creates an artificial monomorphic resp. megamorphic call site. This is possible
 * in Java, because subtyping makes it impossible to determine the concrete method until runtime.
 */
package nl.martijndwars.jits;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Warmup(iterations = 5, time = 1, timeUnit = SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class Inlining {
  public abstract static class A {
    int c1, c2, c3;
    
    public abstract void m();
  }
  
  public static class C1 extends A {
    public void m() {
      c1++;
    }
  }
  
  public static class C2 extends A {
    public void m() {
      c2++;
    }
  }
  
  public static class C3 extends A {
    public void m() {
      c3++;
    }
  }
  
  A[] as;
  
  @Param({"mono", "mega"})
  private String mode;
  
  @Setup
  public void setup() {
    /* fill as with c1, c2, c3, c1, c2, ... */
    as = new A[300];
    boolean mega = mode.equals("mega");
    for (int c = 0; c < 300; c += 3) {
      as[c] = new C1();
      as[c + 1] = mega ? new C2() : new C1();
      as[c + 2] = mega ? new C3() : new C1();
    }
  }
  
  @Benchmark
  public void test() {
    for (A a : as) {
      a.m();
    }
  }
}