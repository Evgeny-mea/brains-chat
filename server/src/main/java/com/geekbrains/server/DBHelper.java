package com.geekbrains.server;

import java.sql.*;

public class DBHelper {
    private static DBHelper instance;
    private static Connection connection;

    private static PreparedStatement findByLoinAndPasword;
    private static PreparedStatement changeNick;

    private DBHelper() {}

    public static DBHelper getInstance() {
        if (instance == null) {
            LoadDriverAndOpenConnection();
            createPreparedStatements();
            instance = new DBHelper();
        }
        return instance;
    }

    private static void LoadDriverAndOpenConnection(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqLite:chat.db");
        } catch (ClassNotFoundException | SQLException e){
            System.err.println("Ошибка открытия соединения с базой данных!");
            e.printStackTrace();
        }
    }
    private static void createPreparedStatements() {
        try{
            findByLoinAndPasword = connection.prepareStatement("SELECT * FROM participant WHERE LOWER(login)=LOWER(?) AND password=?");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public String findByLoginAndPassword(String login, String password){
        ResultSet resultSet = null;
    try {
        findByLoinAndPasword.setString(1,login);
        findByLoinAndPasword.setString(2,password);

        resultSet = findByLoinAndPasword.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("nickname");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        cloneResultSet(resultSet);
    }
    return null;
    }
    private void cloneResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
