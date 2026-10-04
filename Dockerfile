# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM jlesage/baseimage-gui:alpine-3.15-v4

# Set environment variables for the GUI
ENV APP_NAME="EthioTour Connect"
ENV KEEP_APP_RUNNING=1

# Install Java 17 and fonts
RUN apk add --no-cache openjdk17-jre ttf-dejavu fontconfig

WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/ethiotour-connect-1.0-SNAPSHOT.jar /app/app.jar

# Setup the application start script that baseimage-gui uses
RUN echo "#!/bin/sh" > /startapp.sh && \
    echo "exec java -jar /app/app.jar" >> /startapp.sh && \
    chmod +x /startapp.sh
