FROM maven: 3.9-eclipse-temerin-21

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM tomcat:11.0-jdk25

FROM rm -rf /usr/local/tomcat/webapps/*

COPY --from-build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]