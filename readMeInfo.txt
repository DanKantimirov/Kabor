mvn install:install-file -Dfile=D:\teraDriver\REngine.jar -DgroupId=r.proglang  -DartifactId=engine -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=D:\teraDriver\Rserve.jar -DgroupId=r.proglang  -DartifactId=rserve -Dversion=1.0 -Dpackaging=jar

//Database setUp

CREATE TABLE `demand_predictor`.`v_sales_rest` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `whs_id` INT NOT NULL,
  `art_id` INT NOT NULL,
  `day_id` DATE NOT NULL,
  `sale_qnty` DOUBLE NULL,
  `rest_qnty` DOUBLE NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


CREATE TABLE `demand_predictor`.`v_request` (
  `request_id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(45) NOT NULL,
  `send_date_time` DATETIME NOT NULL,
  `response_date_time` DATETIME NULL,
  `status` INT NOT NULL,
  `response_text` VARCHAR(500) NULL,
  `attachment_path` VARCHAR(150) NULL,
  `document_path` VARCHAR(150) NOT NULL,
  PRIMARY KEY (`request_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

ALTER TABLE demand_predictor.v_sales_rest ADD COLUMN  request_id INT;

ALTER TABLE demand_predictor.v_sales_rest ADD CONSTRAINT fk_v_request FOREIGN KEY (request_id) REFERENCES demand_predictor.v_request(request_id) ON UPDATE CASCADE
ON DELETE CASCADE;

//Start Rserve
library(Rserve)
Rserve(debug = FALSE, 6311)

//URL
http://localhost:8090/demand/login.html

Login: analyst
Password: secret