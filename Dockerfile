ARG DIR_NAME
ARG PG_VER
FROM debian:buster
ARG DIR_NAME
ARG PG_VER
RUN apt-get update
RUN apt-get install -yyy apt-utils
RUN apt-get install -yyy mc
RUN apt-get install -yyy openjdk-11-jdk
RUN echo "deb https://dl.bintray.com/sbt/debian/ /" > /etc/apt/sources.list.d/sbt.list
RUN apt-get install -yyy gnupg
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 99E82A75642AC823
RUN apt-get update
RUN apt-get install -yyy sbt
RUN apt-get install -yyy postgresql-${PG_VER}
RUN sed -i "s!#listen_addresses = 'localhost'!listen_addresses = '*'    !" /etc/postgresql/${PG_VER}/main/postgresql.conf
EXPOSE 9000 9443
RUN mkdir /${DIR_NAME}
ADD . /${DIR_NAME}
