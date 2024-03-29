package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ExpediteMetric;
import com.danielcsant.gitlab.model.TeamMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class ExpediteDaoMySqlImpl implements IExpediteDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExpediteDaoMySqlImpl.class);

    @Override
    public boolean upsert(String tableName, List<ExpediteMetric> teamMetrics) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            stm = con.prepareStatement("truncate " + formatTableName(tableName));
            stm.execute();

            stm = con.prepareStatement("insert ignore into " + formatTableName(tableName) + " values(?,?,?,?,?,?,?)");
            int i = 0;
            for (ExpediteMetric newExpediteMetric : teamMetrics) {

                stm.setDate(1, newExpediteMetric.getMetricDate());
                stm.setString(2, newExpediteMetric.getProject());
                stm.setInt(3, newExpediteMetric.getIid());
                stm.setString(4, newExpediteMetric.getTeam());
                stm.setString(5, newExpediteMetric.getTitle());
                stm.setString(6, newExpediteMetric.getUrl());

                if (newExpediteMetric.getResolutionHours() != null) {
                    stm.setDouble(7, newExpediteMetric.getResolutionHours());
                } else {
                    stm.setNull(7, Types.DOUBLE);
                }

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
}
