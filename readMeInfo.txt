1.) Use commands below for adding artefacts to local maven repository. All packages can be downloaded from  https://rforge.net/Rserve/files/

	mvn install:install-file -Dfile=D:\teraDriver\REngine.jar -DgroupId=r.proglang  -DartifactId=engine -Dversion=1.0 -Dpackaging=jar
	mvn install:install-file -Dfile=D:\teraDriver\Rserve.jar -DgroupId=r.proglang  -DartifactId=rserve -Dversion=1.0 -Dpackaging=jar

2.) Use SettingUpDatabase.txt for creating database schema.
3.) Your R instance has to contains packages: Rserve,forecast,GMDH. Just use commands from list below for installation them.

	install.packages("Rserve")
	install.packages("forecast")
	install.packages("GMDH")

4.) After installing all packages start Rserve. 

	library(Rserve)
	Rserve(debug = FALSE, 6311)
	
5.) You might want to change some of settings. File with project's properties is located in src\main\resources\application.properties .

	dataSource.url			-- settings for connection to database
	dataSource.username
	dataSource.password

	r.host					-- settings for connection to R
	r.port

	email.login				-- settings for connection to main service
	email.password
	email.host

6.) Use command below for building project.

	mvn clean package
	
7.) Use command below for running project.

	mvn spring-boot:run
	
8.) The Main page is located in 

	http://localhost:8090/demand/index.html
	
9.) Admin mode pages are located in

	http://localhost:8090/demand/adminSingleMode.html
	http://localhost:8090/demand/adminMultipleMode.html

	Login: analyst
	Password: secret