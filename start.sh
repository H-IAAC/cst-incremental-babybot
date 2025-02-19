#!/bin/bash


echo "I'm here"

cd /home/headless/Desktop/rl_CSR && JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew --configure-on-demand -x check run
