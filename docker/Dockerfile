FROM ubuntu:20.04
MAINTAINER Oliver Bauer <oliver.bauer@tu-dortmund.de>

ENV DEBIAN_FRONTEND=noninteractive

# Install some software
RUN apt-get update && apt-get install -y \
  tzdata \
  melt \
  git \
  maven \
  ffmpeg \
  imagemagick \
  wget \
  x11-apps \
  xvfb \
  tigervnc-scraping-server

# Solution from https://github.com/phusion/passenger-docker/issues/195:
RUN ln -fs /usr/share/zoneinfo/America/New_York /etc/localtime
RUN dpkg-reconfigure --frontend noninteractive tzdata

RUN mkdir -p /home/jMIF
WORKDIR /home/jMIF/

# Java-Stuff
RUN wget --no-check-certificate -c --header "Cookie: oraclelicense=accept-securebackup-cookie" https://download.oracle.com/otn-pub/java/jdk/14.0.1+7/664493ef4a6946b186ff29eb326336a2/jdk-14.0.1_linux-x64_bin.tar.gz
RUN gunzip jdk-14.0.1_linux-x64_bin.tar.gz
RUN tar -xf jdk-14.0.1_linux-x64_bin.tar
ENV JAVA_HOME=/home/jMIF/jdk-14.0.1
RUN export PATH=$PATH:$JAVA_HOME/bin
RUN update-alternatives  --install /usr/bin/java java /home/jMIF/jdk-14.0.1/bin/java 2082

# jMIF-Application
RUN git clone https://github.com/oliverbauer/jMIF.git
WORKDIR /home/jMIF/jMIF/jMIF/
RUN mvn clean install -Dmaven.test.skip

ENV DISPLAY :0
EXPOSE 5900

RUN touch bootstrap.sh
RUN echo '#!/bin/bash' >> bootstrap.sh
RUN echo '' >> bootstrap.sh
RUN echo ' export DISPLAY=0 \' >> bootstrap.sh
RUN echo ' && Xvfb :0 -screen 0 493x476x8 & \' >> bootstrap.sh
RUN echo ' x11vnc -usepw -display :0 -forever -create & \' >> bootstrap.sh
RUN echo ' ffmpeg -version & \' >> bootstrap.sh
RUN echo ' convert -version & \' >> bootstrap.sh
RUN echo 'java -jar /home/jMIF/jMIF/jMIF/gui/swing/target/jmif-swing-gui-1.0-SNAPSHOT-jar-with-dependencies.jar' >> bootstrap.sh
RUN chmod +x bootstrap.sh

CMD ["/home/jMIF/jMIF/jMIF/bootstrap.sh"]

WORKDIR /home/jMIF
