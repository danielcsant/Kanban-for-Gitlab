package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ProjectMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ProjectDaoMySqlImpl implements IProjectDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProjectDaoMySqlImpl.class);

    @Override
    public boolean insert(String tableName, List<ProjectMetric> projectMetrics) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            createTable(formatTableName(tableName), con);
            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
            int i = 0;
            for (ProjectMetric newProjectMetric : projectMetrics) {
                stm.setDate(1, newProjectMetric.getMetricDate());
                stm.setInt(2, newProjectMetric.getOpen());
                stm.setInt(3, newProjectMetric.getToDo());
                stm.setInt(4, newProjectMetric.getDoing());
                stm.setInt(5, newProjectMetric.getDesplegadoEnTest());
                stm.setInt(6, newProjectMetric.getDesplieguePendiente());
                stm.setInt(7, newProjectMetric.getDesplegado());
                stm.setInt(8, newProjectMetric.getClosed());
                stm.setInt(9, newProjectMetric.getNewBugs());
                stm.setInt(10, newProjectMetric.getMasterCoverage());
                stm.setInt(11, newProjectMetric.getNewTasks());
                stm.setString(12, newProjectMetric.getProject());

                stm.addBatch();
                i++;

                if (i % 1000 == 0 || i == projectMetrics.size()) {
                    stm.executeBatch(); // Execute every 1000 items.
                }

            }

            insert=true;
            stm.close();
            con.close();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
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
    public boolean insert(String tableName, ProjectMetric projectMetric) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            createTable(formatTableName(tableName), con);

            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
            stm.setDate(1, projectMetric.getMetricDate());
            stm.setInt(2, projectMetric.getOpen());
            stm.setInt(3, projectMetric.getToDo());
            stm.setInt(4, projectMetric.getDoing());
            stm.setInt(5, projectMetric.getDesplegadoEnTest());
            stm.setInt(6, projectMetric.getDesplieguePendiente());
            stm.setInt(7, projectMetric.getDesplegado());
            stm.setInt(8, projectMetric.getClosed());
            stm.setInt(9, projectMetric.getNewBugs());
            stm.setInt(10, projectMetric.getMasterCoverage());
            stm.setInt(11, projectMetric.getNewTasks());
            stm.setString(12, projectMetric.getProject());

            int i = stm.executeUpdate();
            LOGGER.info(i+" records inserted");
            insert=true;
            stm.close();
            con.close();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
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
