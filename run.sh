#!/usr/bin/env bash

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home
GRAAL_HOME=/Library/Java/JavaVirtualMachines/graalvm-ee-19.2.1/Contents/Home

# Compile and assemble a fat JAR
./gradlew clean shadowJar

# Run the benchmark with Graal
$GRAAL_HOME/bin/java -jar build/libs/benchmarks.jar --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation"

# Run the benchmark with C2
$JAVA_HOME/bin/java -jar build/libs/benchmarks.jar --jvmArgsAppend="-XX:LoopUnrollLimit=1 -XX:-TieredCompilation"

