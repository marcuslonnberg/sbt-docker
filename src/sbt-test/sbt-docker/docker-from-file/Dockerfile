FROM openjdk:8-jre

COPY scala-library-2.11.5.jar /root/scala-library-2.11.5.jar

COPY docker-from-file_2.11-0.1.0.jar /root/docker-from-file_2.11-0.1.0.jar

CMD ["java", "-cp", "/root/*", "simple.Hello"]
