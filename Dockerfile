# Midland Saloon backend - Spring Boot 3.5 on Java 21.
# Build with Maven, run on a slim JRE.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Uploads live on a volume mounted here (UPLOAD_DIR=/data/uploads/ on Railway).
RUN mkdir -p /data/uploads
# Tuned for Render's free plan (512 MB, 0.1 CPU):
#  - MaxRAMPercentage=65 leaves room for metaspace, thread stacks and the JIT
#  - SerialGC: no parallel GC threads fighting over a tenth of a CPU, and the
#    smallest footprint for a heap this size
#  - TieredStopAtLevel=1: C1 only. C2's optimising compiles burn CPU for
#    minutes after every (cold) start - CPU this plan does not have
#  - Xss512k: half the default stack for each of Tomcat's threads
#  - ReservedCodeCacheSize/MaxMetaspaceSize: cap the non-heap regions
#  - ExitOnOutOfMemoryError: restart cleanly instead of limping on
# Override JAVA_OPTS in the dashboard to change any of it without a rebuild.
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:ReservedCodeCacheSize=64m -XX:MaxMetaspaceSize=192m -XX:+ExitOnOutOfMemoryError"
EXPOSE 8083
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
