FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:7460683c40318673e34affeac2cfd40746a5f62e9c0dfa6449ac83e3d95be05e

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

COPY build/libs/dp-behov-pdf-generator-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
