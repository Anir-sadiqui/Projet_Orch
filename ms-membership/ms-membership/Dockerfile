FROM eclipse-temurin:17-jre-alpine


LABEL maintainer="ecommerce-platform"
LABEL service="ms-order"


WORKDIR /app


COPY target/*.jar app.jar


EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]
