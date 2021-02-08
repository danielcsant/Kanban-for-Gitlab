package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.Metric;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

public class MetricDaoMySqlImpl implements IMetricDao {

    private final static Logger LOGGER = Logger.getLogger("com.danielcsant.gitlab.repository.MetricDaoMySqlImpl");

    @Override
    public boolean insert(String tableName, List<Metric> metrics) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            createTable(formatTableName(tableName), con);
            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
            int i = 0;
            for (Metric newMetric : metrics) {
                stm.setDate(1, newMetric.getMetricDate());
                stm.setInt(2, newMetric.getOpen());
                stm.setInt(3, newMetric.getToDo());
                stm.setInt(4, newMetric.getDoing());
                stm.setInt(5, newMetric.getDesplegadoEnTest());
                stm.setInt(6, newMetric.getDesplieguePendiente());
                stm.setInt(7, newMetric.getDesplegado());
                stm.setInt(8, newMetric.getClosed());
                stm.setInt(9, newMetric.getNewBugs());
                stm.setInt(10, newMetric.getMasterCoverage());
                stm.setInt(11, newMetric.getNewTasks());
                stm.setString(12, newMetric.getProject());

                stm.addBatch();
                i++;

                if (i % 1000 == 0 || i == metrics.size()) {
                    stm.executeBatch(); // Execute every 1000 items.
                }

            }

            insert=true;
            stm.close();
            con.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return insert;

    }

    private String formatTableName(String tableName) {
        String tableNameAux = tableName;
        if (tableNameAux.indexOf("/") != -1) {
            tableNameAux = tableNameAux.substring(tableNameAux.indexOf("/") + 1);
        }
        return tableNameAux
                .toLowerCase()
                .replaceAll("-","_");
    }

    @Override
    public boolean insert(String tableName, Metric metric) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            createTable(formatTableName(tableName), con);

            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
            stm.setDate(1, metric.getMetricDate());
            stm.setInt(2, metric.getOpen());
            stm.setInt(3, metric.getToDo());
            stm.setInt(4, metric.getDoing());
            stm.setInt(5, metric.getDesplegadoEnTest());
            stm.setInt(6, metric.getDesplieguePendiente());
            stm.setInt(7, metric.getDesplegado());
            stm.setInt(8, metric.getClosed());
            stm.setInt(9, metric.getNewBugs());
            stm.setInt(10, metric.getMasterCoverage());
            stm.setInt(11, metric.getNewTasks());
            stm.setString(12, metric.getProject());

            int i = stm.executeUpdate();
            LOGGER.info(i+" records inserted");
            insert=true;
            stm.close();
            con.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return insert;
    }

    private void createTable(String tableName, Connection con) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `" + tableName.toLowerCase() + "` (\n" +
                "  `metric_date` date NOT NULL,\n" +
                "  `open` int(11) NOT NULL,\n" +
                "  `to_do` int(11) NOT NULL,\n" +
                "  `doing` int(11) NOT NULL,\n" +
                "  `desplegado_en_test` int(11) NOT NULL,\n" +
                "  `despliegue_pendiente` int(11) NOT NULL,\n" +
                "  `desplegado` int(11) NOT NULL,\n" +
                "  `closed` int(11) NOT NULL,\n" +
                "  `new_bugs` int(11) NOT NULL,\n" +
                "  `master_coverage` int(11) NOT NULL,\n" +
                "  `new_tasks` int(11) NOT NULL,\n" +
                "  `project` varchar(100) NOT NULL\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci";

        Statement stmt = con.createStatement();
        stmt.execute(sqlCreate);
    }
}
