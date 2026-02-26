#criação da imagem docker
FROM tomcat:11.0-jdk21-temurin

#remoção dos arquivos WEBAPPS do tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

#cópia dos arquivos do localhost para a imagem docker
COPY target/app.war /usr/local/tomcat/webapps/ROOT.war

#abrir a porta 8080
EXPOSE 8080

#execução do docker
CMD ["catalina.sh", "run"]