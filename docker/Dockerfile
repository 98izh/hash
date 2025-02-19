FROM openjdk:21-jdk-bullseye

ENV PORT=8080

RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y openssl git && \
    apt-get clean && \ 
    unlink /etc/localtime && \ 
    ln -s /usr/share/zoneinfo/Asia/Amman /etc/localtime

RUN adduser --disabled-password --gecos "" proxidize && \
    mkdir -p /home/proxidize/app && \
    mkdir -p /home/proxidize/logs && \
    mkdir -p /home/proxidize/otel

EXPOSE 8080

COPY entrypoint.sh /home/proxidize/

RUN chmod +x /home/proxidize/entrypoint.sh && \
    chown -R proxidize:proxidize /home/proxidize

USER proxidize

RUN mkdir -p ~/.ssh/ && ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts

WORKDIR /home/proxidize

RUN curl  https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.18.0/jmx_prometheus_javaagent-0.18.0.jar -o /home/proxidize/jmx_prometheus_javaagent.jar && \
RUN curl -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.3.0/opentelemetry-javaagent.jar -o /home/proxidize/otel/opentelemetry-javaagent.jar

COPY config.yaml /home/proxidize/config.yaml

ENV JAVAOPS="-Xms1024m -Xmx1024m -XX:+UseG1GC -XX:ParallelGCThreads=2 -XX:MaxGCPauseMillis=25000 -javaagent:/home/proxidize/otel/opentelemetry-javaagent.jar"

COPY hash.jar /home/proxidize/app/

ENTRYPOINT [ "/home/proxidize/entrypoint.sh" ]