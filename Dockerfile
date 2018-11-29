FROM maven:latest as build
COPY . /tmp/build
WORKDIR /tmp/build
RUN mvn -B -f /tmp/build/pom.xml -s /usr/share/maven/ref/settings-docker.xml clean package

FROM jetty:latest
COPY --from=build /tmp/build/olastic-web/target/Olastic.war /var/lib/jetty/webapps/olastic.war