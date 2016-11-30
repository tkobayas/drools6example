This is a simple example to demonstrate kjar <- main application dependency.

cd ./drools-mvn-kjar
mvn clean install
cd ../drools-mvn-main
mvn clean test

If you have configured a container in kie-server, you can run the REST application

cd ../drools-mvn-rest
mvn clean test
