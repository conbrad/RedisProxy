# Adapted from: https://hub.docker.com/r/hseeberger/scala-sbt/~/dockerfile/

# JDK 8.131
FROM openjdk:8u131

# Env variables
ENV SCALA_VERSION 2.12.6
ENV SBT_VERSION 1.1.6

# Scala expects this file
RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install Scala
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

# Point PROJECT_HOME env to src folder, create app folder
ENV PROJECT_HOME /usr/src
RUN mkdir -p $PROJECT_HOME/app

# Add JAR path to PATH
ENV PATH $PROJECT_WORKPLACE/build/target/universal/stage/bin:$PATH

# Copy everything to app folder and set it as working dir
COPY . $PROJECT_HOME/app
WORKDIR $PROJECT_HOME/app

# Grab dependencies now so they can be reused
RUN sbt update

# HTTP server port
EXPOSE 9000

ENTRYPOINT ["sbt", "run"]