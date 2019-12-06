DROP SCHEMA IF EXISTS `stock_platform`;

CREATE SCHEMA `stock_platform`;

use `stock_platform`;

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(45) DEFAULT NULL,
  `last_name` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `balance` double DEFAULT NULL,
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `property`;

CREATE TABLE `property` (

  `id` int(11) NOT NULL AUTO_INCREMENT,
  `num_stocks` int(11) NOT NULL,
  `stock_name` varchar(45) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,

  PRIMARY KEY (`id`),
  
  
  FOREIGN KEY (`user_id`) 
  REFERENCES `user` (`id`) 
  ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


INSERT INTO `user` VALUES 
  (1,'David','Adams','david@colorado.edu', 10000.00),
  (2,'John','Doe','john@colorado.edu',10000.00),
  (3,'Ajay','Rao','ajay@colorado.edu', 10000.00),
  (4,'Mary','Public','mary@colorado.edu',10000.00),
  (5,'Maxwell','Dixon','max@lcolorado.edu',10000.00);

INSERT INTO `property` VALUES 
  (1, 10, 'GOOG', 1),
  (5, 30, 'AMZN', 1);


SET FOREIGN_KEY_CHECKS = 1;
