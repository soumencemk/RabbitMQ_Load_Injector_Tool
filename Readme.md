# RabbitMQ Load injector tool

![Java CI with Maven](https://github.com/soumencemk/RabbitMQ_Load_Injector_Tool/workflows/Java%20CI%20with%20Maven/badge.svg)
## SETUP
#### 1. Change the [application.properties](./src/main/resources/application.properties) with the proper values
```properties
###################
# Spring config ###
###################
spring.application.name=RabbitMQ-LoadTester
server.port=${SERVER_PORT:8080}
###################
# RabbitMQ Config #
###################
rabbitmq.hosts=${RABBIT_HOSTS:localhost}
rabbitmq.port=${RABBIT_PORT:5672}
################
## SSL Config ##
################
usessl=false
keystore.file=/home/karmaks/val_certs/smcct1-valvm-1.jks
truststore.file=/home/karmaks/val_certs/TrustStore.jks
keystore.pass=${KEYSTORE_PASSWORD:pass}
trustore.pass=${TRUSTORE_PASSWORD:pass}
rabbitmq.user=${RABBIT_USERNAME:guest}
rabbitmq.pass=${RABBIT_PASSWORD:guest}

################
### H2 DATABASE
################
spring.h2.console.enabled=true
spring.h2.console.path=/h2
spring.datasource.url=jdbc:h2:~/loadTester;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=${H2_USERNAME:admin}
spring.datasource.password=${H2_PASSWORD:admin}
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update

```
> Optionally, the changes can be made to the external config file that we are going to create on step - 3. 
#### 2. Build the project -
```bash
$ ./mvnw clean package
```
> you might need to set the `JAVA_HOME` and other env variables for this.

#### 3. copy the application.prorties to current directory -

```bash
$ cp ./src/main/resources/application.properties config.properties
```
> Changes to the properties has to be made in this `config.properties` file itself, if wasn't made already in application.properties. 

#### 4. Execute the [`start.sh`](start.sh) script to start the application.   
#### 5. Execute the [`stop.sh`](stop.sh) script to stop the application. 



