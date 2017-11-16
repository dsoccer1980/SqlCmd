package ua.com.juja.sqlcmd.model;

import com.sun.deploy.util.StringUtils;

import java.sql.*;
import java.util.*;


public class JDBCDatabaseManager implements DatabaseManager {
    private Connection connection;

    @Override
    public List<DataSet> getTableData(String tableName) {
        List<DataSet> result = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName))
        {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnSize = rsmd.getColumnCount();

            while (rs.next()) {
                DataSet dataSet = new DataSetImpl();
                result.add(dataSet);
                for (int i = 0; i < columnSize; i++) {
                    dataSet.put(rsmd.getColumnName(i+1), rs.getObject(i+1));
                }

            }
            return result;
        } catch (SQLException e) {
            e.getMessage();
            return result;
        }
    }

    @Override
    public Set<String> getTableNames() {
        String sqlSelect = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'";
        Set<String> tables = new LinkedHashSet<>();

        try(Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sqlSelect))
        {
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
            return tables;
        } catch (SQLException e) {
            e.getMessage();
            return tables;
        }
    }

    @Override
    public void connect(String database, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Please add jdbc jar to project", e);
        }
        try {
            if (connection!=null){
                connection.close();
            }
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/" + database + "?loggerLevel=OFF", user,
                    password);
        } catch (SQLException e) {
            connection = null;
            throw new RuntimeException(String.format("Cant get connection for model: %s, user: %s, password: %s. ", database, user, password), e);
        }

    }

    @Override
    public void clear(String tableName) throws SQLException{
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM " + tableName);
        }
    }

    @Override
    public void insert(String tableName, DataSet input) throws SQLException{
        try (Statement statement = connection.createStatement()) {
            String tableNames = getNameFormatted(input, "%s,");
            String values = getValuesFormated(input, "'%s',");

            String sql = "INSERT INTO " + tableName + " (" + tableNames + ") VALUES(" + values + ")";
            statement.executeUpdate(sql);

        }
    }

    @Override
    public void update(String tableName, DataSet input, DataSet condition) throws SQLException{
        String tableNames = getNameFormatted(input, "%s = ?,");
        String conditionNames = getNameFormatted(condition, "%s = ?,");

        String sql = "Update " + tableName + " SET " + tableNames + " Where " + conditionNames;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object value : input.getValues()) {
                ps.setObject(index, value);
                index++;
            }
            for (Object value : condition.getValues()) {
                ps.setObject(index, value);
                index++;
            }
            ps.executeUpdate();
        }
    }

    private String getValuesFormated(DataSet input, String format) {
        StringBuilder values = new StringBuilder("");
        for (Object value : input.getValues()) {
            values.append(String.format(format, value));
        }

        return values.substring(0, values.length() - 1);
    }

//    @Override  //TODO
//    public void update(String tableName, int id, DataSet newValue) {
//        String tableNames = getNameFormatted(newValue, "%s = ?,");
//        String sql = "Update " + tableName + " SET " + tableNames + " Where id=?";
//
//        try (PreparedStatement ps = connection.prepareStatement(sql))
//        {
//            int index = 1;
//            for (Object value : newValue.getValues()) {
//                ps.setObject(index, value);
//                index++;
//            }
//
//            ps.setInt(index, id);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void drop(String tableName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE " + tableName);
        }
    }

    @Override
    public void create(String tableName, List<String> columnList) throws SQLException {
        String columnNamesFormatted =  StringUtils.join(columnList, " text,") + " text";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format("CREATE TABLE " + tableName + "(%s)",columnNamesFormatted));
        }
    }

    @Override
    public void delete(String tableName, List<String> columnAndValue) throws SQLException {
        String column = columnAndValue.get(0);
        String value = columnAndValue.get(1);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format("DELETE FROM %s WHERE %s='%s'", tableName, column, value));
        }
    }

    @Override
    public Set<String> getTableColumns(String tableName) {
        String sqlSelect = "SELECT * FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '" + tableName + "'";
        Set<String> tables = new LinkedHashSet<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlSelect))
        {
            while (rs.next()) {
                tables.add(rs.getString("column_name"));
            }
            return tables;
        } catch (SQLException e) {
            e.printStackTrace();
            return tables;
        }
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    private String getNameFormatted(DataSet newValue, String format) {
        StringBuilder string = new StringBuilder("");
        for (String name : newValue.getNames()) {
            string.append(String.format(format, name));
        }
        return string.substring(0, string.length() - 1);
    }

    @Override
    public boolean isTableExists(String tableName) {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
