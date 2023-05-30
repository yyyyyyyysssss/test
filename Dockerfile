# 指定基础镜像
FROM ys0921/jdk:jdk8v1.0

MAINTAINER ys

RUN touch /var/log/app.log && touch /var/log/java_gc.log \
    && chmod 777 /var/log/app.log && chmod 777 /var/log/java_gc.log

RUN useradd -d /home/app -ms /bin/bash app

USER app

WORKDIR /app

ENV SPRING_PROFILE_ACTIVE=dev

ADD ./work-springboot.jar /app

CMD ["sh","-c","java -jar -Xloggc:/var/log/java_gc.log /app/work-springboot.jar --spring.profiles.active=$SPRING_PROFILE_ACTIVE"]

