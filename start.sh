#!/bin/bash
nohup java -jar -Djava.security.egd=file:/dev/./urandom target/RabbitMQ-Load-Injector-1.0.jar --spring.config.location=config.properties > out.log 2>&1 &
echo $! > app.pid