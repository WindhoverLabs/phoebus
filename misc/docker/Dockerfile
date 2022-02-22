FROM ubuntu:18.04
RUN apt update
RUN apt install -y protobuf-compiler
RUN apt-get install -y openjdk-11-jdk
RUN apt-get install -y maven
RUN apt-get install -y openjfx
RUN echo "export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/" >> /root/.bashrc
COPY ./. /
