package nl.martijndwars.jits;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openjdk.jmh.annotations.CompilerControl.Mode.DONT_INLINE;

@Warmup(iterations = 5, time = 1, timeUnit = SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class Vectorization {
  
  public static final int SIZE = 2048;
  
  public int[] a;
  public int[] b;
  public byte[] c;
  
  @Setup(Level.Iteration)
  public void setup() {
    a = new int[SIZE];
    b = new int[SIZE];
    c = new byte[SIZE];
    Random random = new Random();
    
    for (int i = 0; i < SIZE; i++) {
      a[i] = random.nextInt(10);
      b[i] = random.nextInt(10);
      c[i] = (byte) (random.nextInt(26) + 65);
    }
  }
  
  /**
   * The simplest kind of auto-vectorization loop: no control-flow, no data dependencies, single entry, single exit.
   *
   * Compiler   Benchmark             Mode  Cnt    Score   Error  Units
   * -----------------------------------------------------------------
   * Gr 19.2.1  Vectorization.simple  avgt   10  618.237 ± 2.033  ns/op
   * Gr 20.0.0  Vectorization.simple  avgt   10  496.564 ± 6.666  ns/op
   * C2         Vectorization.simple  avgt   10  471.502 ± 2.643  ns/op
   * 
   * @param blackhole
   */
  @Benchmark
  public void simple(Blackhole blackhole) {
    int[] c = new int[SIZE];
    
    for (int i = 0; i < 1024; i++) {
      c[i] = a[i] + b[i];
    }
    
    blackhole.consume(c);
  }
  
  /**
   * A loop with a branch. This is meant to simulate decoding of a UTF8 string.
   *
   * Compiler   Benchmark             Mode  Cnt    Score    Error  Units
   * -----------------------------------------------------------------
   * Gr 19.2.1  Vectorization.branch  avgt   10  813.716 ± 21.986  ns/op
   * Gr 20.0.0  Vectorization.branch  avgt   10  337.390 ± 86.021  ns/op
   * C2         Vectorization.branch  avgt   10  532.758 ±  3.915  ns/op
   * 
   * Whereas Graal 19.2.1 was way behind C2, Graal 20.0.0 beats C2 by a lot.
   */
  @Benchmark
  public char[] decodeAscii() {
    char[] z = new char[2048];
    int i = 0;
    
    for (; i < 1024; i++) {
      if (c[i] < 0) {
        return decodeGeneric(c);
      }
      
      z[i] = (char) c[i];
    }
    
    return z;
  }
  
  @CompilerControl(DONT_INLINE)
  private char[] decodeGeneric(byte[] z) {
    return null;
  }
  
  
  /**
   * Sum all 0 < a[i] < 10 
   *
   * Compiler   Benchmark             Mode  Cnt    Score      Error  Units
   * ---------------------------------------------------------------------
   * Gr 19.2.1  Vectorization.reduction  avgt   10  95.667  ± 9.116  ns/op
   * Gr 20.0.0  Vectorization.reduction  avgt   10  141.643 ± 3.297  ns/op
   * C2         Vectorization.reduction  avgt   10  524.020 ± 0.789  ns/op
   * 
   * Graal 20.0.0 is slower than Graal 19.2.1, but both Graal versions beat C2 by a lot.
   * 
   * @param blackhole
   */
  @Benchmark
  public void reduceSum(Blackhole blackhole) {
    int sum = 0;
    for (int x : a) {
      sum += x;
    }
    blackhole.consume(sum);
  }
  
  /**
   * Check if all integers are positive. Simulates checking if a UTF-8 byte[] contains only ASCII characters.
   *
   * Compiler   Benchmark             Mode  Cnt    Score      Error  Units
   * ---------------------------------------------------------------------
   * Gr 19.2.1  Vectorization.reduceBoolean  avgt   10  954.601 ± 269.023  ns/op
   * Gr 20.0.0  Vectorization.reduceBoolean  avgt   10  214.948 ± 64.483  ns/op
   * C2         Vectorization.reduceBoolean  avgt   10  451.218 ± 83.527  ns/op
   * 
   * Graal 19.2.1 was 2x slower than C2, but Graal 20.0.0 is 2x faster than C2.
   * 
   * @param blackhole
   */
  @Benchmark
  public void reduceBoolean(Blackhole blackhole) {
    boolean onlyAscii = true;
    
    for (int x : a) {
      if (x < 0) {
        onlyAscii = false;
        break;
      }
    }
    
    blackhole.consume(onlyAscii);
  }
}
