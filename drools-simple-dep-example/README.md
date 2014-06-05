This is a simple example to demonstrate jar <- kjar <- main application dependency.

cd drools-simple-dep-jar
mvn clean install
cd ../drools-simple-dep-kjar
mvn clean install
cd ../drools-simple-dep-main
mvn test
