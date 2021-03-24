package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ProjectMetric;
import com.danielcsant.gitlab.model.TestCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CoverageDaoMySqlImpl implements ICoverageDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(CoverageDaoMySqlImpl.class);

    @Override
    public boolean insert(String tableName, List<TestCoverage> testCoverageList) {
        boolean insert = false;

        PreparedStatement stm= null;
        Connection con=null;

        try {
            con = MysqlConnection.connect();
            stm = con.prepareStatement("insert into " + formatTableName(tableName) + " values(?,?,?)");
            int i = 0;
            for (TestCoverage testCoverage : testCoverageList) {
                stm.setDate(1, testCoverage.getMetricDate());
                stm.setString(2, testCoverage.getProject());
                stm.setString(3, testCoverage.getCoverage());

                stm.addBatch();
                i++;

                if (i % 1000 == 0 || i == testCoverageList.size()) {
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
