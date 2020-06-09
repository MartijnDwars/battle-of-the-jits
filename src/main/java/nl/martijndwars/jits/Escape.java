package nl.martijndwars.jits;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class Escape {
  int x;
  boolean flag;
  
  @Setup(Level.Iteration)
  public void shake() {
    flag = ThreadLocalRandom.current().nextBoolean();
  }
  
  @Benchmark // C2 & Graal perform scalar replacement
  public void single(Blackhole blackhole) {
    for (int i = 0; i < 300; i++) {
      MyObject o = new MyObject(x);
  
      blackhole.consume(o.x);
    }
  }
  
  @Benchmark // C2 won't perform scalar replacement, but Graal will?
  public void split(Blackhole blackhole) {
    for (int i = 0; i < 300; i++) {
      MyObject o;
      
      if (flag) {
        o = new MyObject(x);
      } else {
        o = new MyObject(x);
      }
  
      blackhole.consume(o.x);
    }
  }
  
  static class MyObject {
    final int x;
    
    public MyObject(int x) {
      this.x = x;
    }
  }
}
