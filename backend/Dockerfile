# Etapa 1: build da aplicação usando Gradle Wrapper
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copia os arquivos necessários para o build
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Dá permissão de execução ao gradlew
RUN chmod +x gradlew

# Faz o build do projeto sem rodar testes
RUN ./gradlew bootJar --no-daemon -x test

# Etapa 2: imagem final somente com o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
