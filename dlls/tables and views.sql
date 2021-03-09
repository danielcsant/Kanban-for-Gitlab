
CREATE TABLE `project` (
  `metric_date` date NOT NULL,
  `open` int(11) NOT NULL,
  `to_do` int(11) NOT NULL,
  `doing` int(11) NOT NULL,
  `desplegado_en_test` int(11) NOT NULL,
  `despliegue_pendiente` int(11) NOT NULL,
  `desplegado` int(11) NOT NULL,
  `closed` int(11) NOT NULL,
  `new_expedites` int(11) DEFAULT NULL,
  `master_coverage` int(11) DEFAULT NULL,
  `new_tasks` int(11) DEFAULT NULL,
  `project` varchar(100) COLLATE latin1_spanish_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;


CREATE VIEW metrics.global_coverage AS
SELECT p.metric_date , p.project , p.master_coverage
FROM metrics.project p
WHERE p.metric_date = (SELECT MAX( p.metric_date ) FROM metrics.project p);

CREATE TABLE `team` (
  `metric_date` date NOT NULL,
  `open` int(11) NOT NULL,
  `week_1` int(11) NOT NULL,
  `week_2` int(11) NOT NULL,
  `to_do` int(11) NOT NULL,
  `doing` int(11) NOT NULL,
  `desplegado_en_test` int(11) NOT NULL,
  `despliegue_pendiente` int(11) NOT NULL,
  `desplegado` int(11) NOT NULL,
  `closed` int(11) NOT NULL,
  `team` varchar(100) COLLATE latin1_spanish_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci

CREATE TABLE `expedite` (
  `metric_date` date NOT NULL,
  `project` varchar(100) COLLATE latin1_spanish_ci NOT NULL,
  `iid` int(11) NOT NULL,
  `team` varchar(100) COLLATE latin1_spanish_ci DEFAULT NULL,
  `title` varchar(200) COLLATE latin1_spanish_ci DEFAULT NULL,
  `url` varchar(200) COLLATE latin1_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`project`,`iid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci
