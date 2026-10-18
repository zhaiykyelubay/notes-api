
# Stage 1: Build with JDK 21
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY src/main/ src/main/
RUN mkdir -p /out && find src/main -name '*.java' -print0 | xargs -0 javac -d /out

# Stage 2: Run with JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /out/ /app/
ENV PORT=8080
EXPOSE 8080
USER 1000:1000
CMD ["java", "-cp", "/app", "notes.NotesApp"]
