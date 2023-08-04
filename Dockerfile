FROM gradle:8.2.1-jdk17-alpine AS builder
COPY . /work
WORKDIR /work
RUN gradle installDist

FROM eclipse-temurin:19-alpine
COPY --from=builder /work/app/build/install/app /app
COPY init.sql /app/
WORKDIR /app
ENTRYPOINT /app/bin/app