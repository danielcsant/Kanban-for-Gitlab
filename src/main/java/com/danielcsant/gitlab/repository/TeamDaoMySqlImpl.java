package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ProjectMetric;
import com.danielcsant.gitlab.model.TeamMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TeamDaoMySqlImpl implements ITeamDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(TeamDaoMySqlImpl.class);

    @Override
    public boolean insert(String tableName, List<TeamMetric> teamMetrics) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
            int i = 0;
            for (TeamMetric newTeamMetric : teamMetrics) {
                stm.setDate(1, newTeamMetric.getMetricDate());
                stm.setInt(2, newTeamMetric.getOpen());
                stm.setInt(3, newTeamMetric.getWeek1());
                stm.setInt(4, newTeamMetric.getWeek2());
                stm.setInt(5, newTeamMetric.getToDo());
                stm.setInt(6, newTeamMetric.getDoing());
                stm.setInt(7, newTeamMetric.getDesplegadoEnTest());
                stm.setInt(8, newTeamMetric.getDesplieguePendiente());
                stm.setInt(9, newTeamMetric.getDesplegado());
                stm.setInt(10, newTeamMetric.getClosed());
                stm.setInt(11, newTeamMetric.getNewBugs());
                stm.setString(12, newTeamMetric.getProject());

                stm.addBatch();
                i++;

                if (i % 1000 == 0 || i == teamMetrics.size()) {
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
    public boolean insert(String tableName, TeamMetric newTeamMetric) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();

            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
            stm.setDate(1, newTeamMetric.getMetricDate());
            stm.setInt(2, newTeamMetric.getOpen());
            stm.setInt(3, newTeamMetric.getWeek1());
            stm.setInt(4, newTeamMetric.getWeek2());
            stm.setInt(5, newTeamMetric.getToDo());
            stm.setInt(6, newTeamMetric.getDoing());
            stm.setInt(7, newTeamMetric.getDesplegadoEnTest());
            stm.setInt(8, newTeamMetric.getDesplieguePendiente());
            stm.setInt(9, newTeamMetric.getDesplegado());
            stm.setInt(10, newTeamMetric.getClosed());
            stm.setInt(11, newTeamMetric.getNewBugs());
            stm.setString(12, newTeamMetric.getProject());

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
}
