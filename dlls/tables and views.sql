
CREATE TABLE `project` (
  `metric_date` date NOT NULL,
  `open` int(11) NOT NULL,
  `to_do` int(11) NOT NULL,
  `doing` int(11) NOT NULL,
  `desplegado_en_test` int(11) NOT NULL,
  `despliegue_pendiente` int(11) NOT NULL,
  `desplegado` int(11) NOT NULL,
  `closed` int(11) NOT NULL,
  `new_bugs` int(11) DEFAULT NULL,
  `master_coverage` int(11) DEFAULT NULL,
  `new_tasks` int(11) DEFAULT NULL,
  `project` varchar(100) COLLATE latin1_spanish_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;


CREATE VIEW metrics.global_coverage AS
SELECT p.metric_date , p.project , p.master_coverage
FROM metrics.project p
WHERE p.metric_date = (SELECT MAX( p.metric_date ) FROM metrics.project p);
