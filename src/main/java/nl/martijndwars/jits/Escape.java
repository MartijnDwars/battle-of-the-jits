package nl.martijndwars.jits;

import org.openjdk.jmh.annotations.*;
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
public class Escape {
  int[] r;
  
  @Setup
  public void setup() {
    Random random = new Random();
    r = new int[300];
    
    for (int i = 0; i < 300; i++) {
      r[i] = random.nextInt(2); // r[i] ∈ [0, 1]
    }
  }
  
  @Benchmark
  public int noEscape() {
    int sum = 0;
    
    for (int i = 0; i < 300; i++) {
      Circle circle = new Circle(r[i]);
      
      if (circle.r > 1) {
        sum += circle.r;
      }
    }
    
    return sum;
  }
  
  /**
   * The circles are initialized with a random value of 0 or 1. The object escapes if the value is
   * greater than 1, which is never the case.
   *
   * TODO: Benchmark doens't show Graal is much better?
   */
  @Benchmark
  public int branchEscape(Blackhole blackhole) {
    int sum = 0;
  
    for (int i = 0; i < 300; i++) {
      Circle circle = new Circle(r[i]);
      
      if (circle.r > 2) {
        sum += circleEscapes(circle);
      } else {
        sum += circle.r;
      }
    }
  
    return sum;
  }
  
  @CompilerControl(DONT_INLINE)
  private int circleEscapes(Circle circle) {
    return circle.r;
  }
  
  private static class Circle {
    public int r;
    
    public Circle(int r) {
      this.r = r;
    }
  
    @Override
    public synchronized boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      
      Circle circle = (Circle) o;
      
      return r == circle.r;
    }
  }
}