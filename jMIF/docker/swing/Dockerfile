FROM ubuntu:18.04
MAINTAINER Oliver Bauer <oliver.bauer@tu-dortmund.de>

ENV DEBIAN_FRONTEND=noninteractive
# Solution from https://github.com/phusion/passenger-docker/issues/195:
RUN apt-get update && apt-get install -y tzdata
RUN ln -fs /usr/share/zoneinfo/America/New_York /etc/localtime
RUN dpkg-reconfigure --frontend noninteractive tzdata

# TODO Install packages for building melt, since "apt-get install melt" installs old version

ENV HOME /tmp

RUN apt-get install melt -y
RUN apt-get install git -y
RUN apt-get install maven -y
RUN mkdir -p /home/jMIF
# clone locally before invoking the script to build a docker file!!!!
# TODO don't do this... better: clone from started image
COPY jMIF/ /home/jMIF/
WORKDIR /home/jMIF/

# local file!
# TODO better: Download from started image
COPY jdk-14.0.1_linux-x64_bin.tar.gz /home/jMIF/
RUN gunzip jdk-14.0.1_linux-x64_bin.tar.gz
RUN tar -xf jdk-14.0.1_linux-x64_bin.tar
RUN ls -la /home/jMIF/jdk-14.0.1/
ENV JAVA_HOME=/home/jMIF/jdk-14.0.1
# TODO not sure if the following is necessary...
RUN export PATH=$PATH:$JAVA_HOME/bin
RUN mvn clean install -Dmaven.test.skip

WORKDIR /home/jMIF
