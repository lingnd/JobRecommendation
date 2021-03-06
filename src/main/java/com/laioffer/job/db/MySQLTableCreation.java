package com.laioffer.job.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreation {
    public static void main(String[] args) {
        try {
            //step 1 connect to MySQL
            System.out.println("Connecting to " + MySQLDBUtil.URL);
            //用reflection的方式创建JDBC instance，singleton的方式创建的，不需要变量名，不要深究，记住这么用的就行了
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);

            if (conn == null) {
                return;
            }

            // Step 2 Drop tables in case they exist.
            // 删除table，如果有就删除，有一个clean start，以前的data都不要了。
            Statement statement = conn.createStatement(); //sql的statement
            String sql = "DROP TABLE IF EXISTS keywords";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS history";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);

            //step 3 create new tables
            //VARCHAR255是column宽度
            sql = "CREATE TABLE items ("
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "name VARCHAR(255),"
                    + "address VARCHAR(255),"
                    + "image_url VARCHAR(255),"
                    + "url VARCHAR(255),"
                    + "PRIMARY KEY (item_id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "PRIMARY KEY (user_id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE keywords ("
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "keyword VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (item_id, keyword),"
                    + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE history ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (user_id, item_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id),"
                    + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
                    + ")";
            statement.executeUpdate(sql);

            //step 4 insert fake user
            sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
            statement.executeUpdate(sql);

            conn.close();
            System.out.println("Import done successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

