#!/usr/bin/env bash

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home
GRAAL_HOME=/Library/Java/JavaVirtualMachines/graalvm-ee-19.2.1/Contents/Home

# Compile and assemble a fat JAR
./gradlew clean shadowJar

# Escape
$GRAAL_HOME/bin/java -jar build/libs/benchmarks.jar Escape --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation" -prof dtraceasm
#$JAVA_HOME/bin/java -jar build/libs/benchmarks.jar Escape --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation"

# Inlining
#$GRAAL_HOME/bin/java -jar build/libs/benchmarks.jar Inlining --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation" -prof gc
#$JAVA_HOME/bin/java -jar build/libs/benchmarks.jar Inlining --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation" -prof gc

# Symbolic
#$GRAAL_HOME/bin/java -jar build/libs/benchmarks.jar Symbolic.simpleRandom --jvmArgsAppend="-XX:LoopUnrollLimit=0 -XX:-TieredCompilation -XX:-UseCompressedOops" -prof dtraceasm
#$JAVA_HOME/bin/java -jar build/libs/benchmarks.jar Symbolic.simpleRandom --jvmArgsAppend="-XX:LoopUnrollLimit=0 -XX:-TieredCompilation -XX:-UseCompressedOops" -prof dtraceasm

# DataFlow
#$GRAAL_HOME/bin/java -jar build/libs/benchmarks.jar DataFlow.single -prof dtraceasm
#$JAVA_HOME/bin/java -jar build/libs/benchmarks.jar DataFlow.single -prof dtraceasm

# JVM flags explanation:
# -XX:LoopUnrollLimit=1: this will block loop unrolling from complicating the disssembly.
# -XX:-TieredCompilation: this disables tiered compilation, which guarantees compilation with the final optimizing compiler.
# -XX:-UseCompressedOops: this disabled compressed references, making assembly easier to read.
# -XX:TieredStopAtLevel=1: has HotSpot stop tiered compilation at level 1 (= C1).

# JMH flags explanation:
# -prof gc: Use the gc profiler
# -prof perfasm / -prof dtraceasm: Print assembly of the hottest region. Use perfasm on Linux, dtraceasm on macOS.
