FROM alpine AS builder

RUN mkdir -p /opt/cprof && \
  wget -q -O- https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz \
  | tar xzv -C /opt/cprof

FROM gcr.io/distroless/java21

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

COPY --from=builder /opt/cprof /opt/cprof
COPY build/libs/dp-behov-pdf-generator-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]