#!/usr/bin/env bash

# Stop execution if a command or pipeline has an error
set -e

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home
G1921_HOME=/Library/Java/JavaVirtualMachines/graalvm-ee-19.2.1/Contents/Home
G2000_HOME=/Library/Java/JavaVirtualMachines/graalvm-ee-java8-20.0.0/Contents/Home
JAR=build/libs/benchmarks.jar

# Compile and assemble a fat JAR
./gradlew clean shadowJar

# Escape
#$G1921_HOME/bin/java -jar $JAR Escape --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation" -prof dtraceasm
#$JAVA_HOME/bin/java -jar $JAR Escape --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation"

# Inlining
#$G1921_HOME/bin/java -jar $JAR Inlining --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation" -pmode=mega -prof dtraceasm
#$JAVA_HOME/bin/java -jar $JAR Inlining --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation" -pmode=mega -prof dtraceasm

# Dataflow
#$G1921_HOME/bin/java -jar $JAR Dataflow.random # --jvmArgsAppend="-XX:LoopUnrollLimit=0 -XX:-TieredCompilation -XX:-UseCompressedOops" -prof dtraceasm
#$JAVA_HOME/bin/java -jar $JAR Dataflow.random # --jvmArgsAppend="-XX:LoopUnrollLimit=0 -XX:-TieredCompilation -XX:-UseCompressedOops" -prof dtraceasm

# Escape
#$G1921_HOME/bin/java -jar $JAR Escape.split -prof dtraceasm
#$JAVA_HOME/bin/java -jar $JAR Escape.split -prof dtraceasm

# Vectorization
#$JAVA_HOME/bin/java -jar $JAR Vectorization.decodeAscii -prof dtraceasm
$G2000_HOME/bin/java -jar $JAR Vectorization.reduceBoolean -prof dtraceasm





# JVM flags explanation:
# -XX:LoopUnrollLimit=1: this will block loop unrolling from complicating the disssembly.
# -XX:-TieredCompilation: this disables tiered compilation, which guarantees compilation with the final optimizing compiler.
# -XX:-UseCompressedOops: this disabled compressed references, making assembly easier to read.
# -XX:TieredStopAtLevel=1: has HotSpot stop tiered compilation at level 1 (= C1).

# JMH flags explanation:
# -prof gc: Use the gc profiler
# -prof perfasm / -prof dtraceasm: Print assembly of the hottest region. Use perfasm on Linux, dtraceasm on macOS.
