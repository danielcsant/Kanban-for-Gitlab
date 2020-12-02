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
    public boolean insert(String tableName, List<Metric> metric) {

        boolean insert = false;

        Statement stm= null;
        Connection con=null;

        String sql="INSERT INTO gitlab values (NULL)";

        try {
            con = MysqlConnection.connect();
            createTable(tableName, con);
            stm = con.createStatement();
            stm.execute(sql);
            insert=true;
            stm.close();
            con.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return insert;

    }

    @Override
    public boolean insert(String tableName, Metric metric) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();

            stm = con.prepareStatement("insert into " + tableName + " values(?,?,?,?,?,?,?,?,?,?)");
            stm.setDate(1, metric.getMetricDate());
            stm.setInt(2, metric.getOpen());
            stm.setInt(3, metric.getToDo());
            stm.setInt(4, metric.getDoing());
            stm.setInt(5, metric.getDesplegadoEnTest());
            stm.setInt(6, metric.getDesplieguePendiente());
            stm.setInt(7, metric.getDesplegado());
            stm.setInt(8, metric.getNewBugs());
            stm.setInt(9, metric.getMasterCoverage());
            stm.setInt(10, metric.getNewTasks());

            createTable(tableName, con);
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
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
                "  `metric_date` date NOT NULL,\n" +
                "  `open` int(11) NOT NULL,\n" +
                "  `to_do` int(11) NOT NULL,\n" +
                "  `doing` int(11) NOT NULL,\n" +
                "  `desplegado_en_test` int(11) NOT NULL,\n" +
                "  `despliegue_pendiente` int(11) NOT NULL,\n" +
                "  `desplegado` int(11) NOT NULL,\n" +
                "  `new_bugs` int(11) NOT NULL,\n" +
                "  `master_coverage` int(11) NOT NULL,\n" +
                "  `new_tasks` int(11) NOT NULL\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci";

        Statement stmt = con.createStatement();
        stmt.execute(sqlCreate);
    }
}
