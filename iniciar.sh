#!/usr/bin/env bash
cd "$(dirname "$0")"
# O projeto compila com Java 21: usa um JDK 21 se achar um com compilador
for j in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-21-openjdk-crac-amd64 /usr/lib/jvm/*21*; do
  if [ -x "$j/bin/javac" ]; then
    export JAVA_HOME="$j"
    export PATH="$JAVA_HOME/bin:$PATH"
    break
  fi
done
echo "Java em uso: $(java -version 2>&1 | head -1)"
( cd backend/metalab/backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local ) &
( cd frontend && npm run dev ) &
trap 'kill 0' EXIT
wait
