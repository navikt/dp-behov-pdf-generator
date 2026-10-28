FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:17d2ae5e06f0f9605119f835e25ec183bca8e882394b1316b4298127c40a9d77

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

COPY build/libs/dp-behov-pdf-generator-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
