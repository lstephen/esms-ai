FROM maven:3-jdk-8
MAINTAINER Levi Stephen <levi.stephen@gmail.com>

COPY . /usr/src/esms-ai

WORKDIR /usr/src/esms-ai

RUN mvn -B clean install -Dgpg.skip=true
