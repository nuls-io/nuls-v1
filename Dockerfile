FROM maven:3.5.4-jdk-8

RUN apt-get update && \
    apt-get -y install \
    openjfx

WORKDIR /tmp/nuls

ADD . .

RUN mvn clean package
RUN mkdir /nuls
RUN tar -zxvf client-module/client/target/nuls-node.tar.gz -C /nuls

WORKDIR /nuls

RUN rm -rf /tmp/nuls

ADD docker-entrypoint.sh ./entry.sh
RUN chmod +x ./entry.sh

VOLUME /nuls/data /nuls/logs

EXPOSE 8011/tcp
EXPOSE 8013/tcp
EXPOSE 8013/udp
ENTRYPOINT ["./entry.sh"]
CMD ["start"]
