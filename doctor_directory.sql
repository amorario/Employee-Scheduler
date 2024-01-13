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
	(1,'Joe','Burrow', 'jburrow@nfl.com', 0, 0, 0, ''),
	(2,'Jamarr','Chase', 'jchase@nfl.com', 0, 0, 0, ''),
	(3,'Josh','Jacobs', 'jjacobs@nfl.com', 0, 0, 0, ''),
	(4,'Damien','Pierce', 'dpierce@nfl.com', 0, 0, 0, ''),
	(5,'Justin','Jefferson', 'jjefferson@nfl.com', 0, 0, 0, ''),
    (6,'Devonte','Smith', 'dsmith@nfl.com', 0, 0, 0, ''),
	(7,'Dallas','Goedert', 'dgoedert@nfl.com', 0, 0, 0, ''),
	(8,'Tyler','Bass', 'tbass@nfl.com', 0, 0, 0, ''),
	(9,'Mike','Evans', 'mevans@nfl.com', 0, 0, 0, '');

    