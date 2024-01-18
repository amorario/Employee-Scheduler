CREATE DATABASE  IF NOT EXISTS `doctor_directory`;
USE `doctor_directory`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `employee`;

CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(45) DEFAULT NULL,
  `last_name` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `month_set` BIT DEFAULT 0,
  `shifts_amount` int DEFAULT 0,
  `weekends` int DEFAULT 0,
  `days_off` varchar(255) DEFAULT NULL,
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `shift`;
CREATE TABLE `shift` (
  `id` int NOT NULL AUTO_INCREMENT,
  `scall` varchar(45) DEFAULT NULL,
  `date` DATETIME DEFAULT NULL,
  `employee_id` int DEFAULT NULL,

  PRIMARY KEY (`id`),

  KEY `FK_EMPLOYEE_ID_idx` (`employee_id`),

  CONSTRAINT `FK_EMPLOYEE` 
  FOREIGN KEY (`employee_id`) 
  REFERENCES `employee` (`id`) 

  ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `employee` VALUES 
	(1,'Joe','Burrow', 'jburrow@nfl.com', 0, 16, 0, '15 22 23 24 25 26'),
	(2,'Jamarr','Chase', 'jchase@nfl.com', 0, 16, 0, '27 28 29 30 31'),
    (3,'Tyler','Bass', 'tbass@nfl.com', 0, 16, 0, '29 30 31'),
	(4,'Josh','Jacobs', 'jjacobs@nfl.com', 0, 7, 0, '1 2 3 4 5 6 7 8 9 10 11 18'),
	(5,'Mike','Evans', 'mevans@nfl.com', 0, 17, 0, '11 18 22 23 24 25 26 27'),
	(6,'Damien','Pierce', 'dpierce@nfl.com', 0, 18, 0, '1 2 3 21 22 23 24 25 26'),
    (7,'Devonte','Smith', 'dsmith@nfl.com', 0, 18, 0, '28 29 30 31'),
	(8,'Dallas','Goedert', 'dgoedert@nfl.com', 0, 13, 0, '27 28 29 30 31'),
	(9,'Justin','Jefferson', 'jjefferson@nfl.com', 0, 3, 0, '1');
	

    