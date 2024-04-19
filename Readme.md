# RabbitMQ Load injector tool

![Travis CI](https://travis-ci.com/soumencemk/RabbitMQ_Load_Injector_Tool.svg?branch=main)
## SETUP
#### 1. Change the [application.properties](./src/main/resources/application.properties) with the proper values
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