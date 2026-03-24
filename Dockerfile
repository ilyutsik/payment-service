FROM amazoncorretto:21 AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon


FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8084
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","--add-opens","java.base/java.math=ALL-UNNAMED","-jar","app.jar"]