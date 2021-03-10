

package com.laioffer.job.db;

import com.laioffer.job.entity.Item;

import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MySQLConnection {
    private Connection conn;

    public MySQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Implement setFavoriteItems 写
    unsetFavoriteItems 删除
    saveItems 把一个item存进item table. 也把keywords存进keyword table

    Syntax for INSERT
    INSERT INTO table_name (column1, column2, column3, ...) VALUES (value1, value2, value3, ...);

    Syntax for DELETE
    DELETE FROM table_name WHERE condition;
    */
    public void setFavoriteItems(String userId, Item item) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }
        saveItem(item);
        String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unsetFavoriteItems(String userId, String itemId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }
        String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";

        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId); //index从1开始，对应上面的？问号
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveItem(Item item) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getLocation());
            statement.setString(4, item.getCompanyLogo());
            statement.setString(5, item.getUrl());
            statement.executeUpdate();

            sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
            statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            for (String keyword : item.getKeywords()) {
                statement.setString(2, keyword);
                //不能放在loop外面， 如果放在外面，只会加最后一个keyword
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    reads data from the history table.
    Syntax for SELECT:   SELECT column1, column2, … FROM table_name;
    */
    public Set<String> getFavoriteItemIds(String userId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return Collections.emptySet(); //global singleton for safety
        }

        Set<String> favoriteItems = new HashSet<>();

        try {
            String sql = "SELECT item_id FROM history WHERE user_id = ?";

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();

            //相当于iterator， 没有元素就return false
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItems.add(itemId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favoriteItems;
    }

    //Implement getFavoriteItems() and getKeywords() methods to read detailed data for a given item.
    public Set<Item> getFavoriteItems(String userId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return new HashSet<>();
        }

        Set<Item> favoriteItems = new HashSet<>();
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);

        String sql = "SELECT * FROM items WHERE item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    favoriteItems.add(new Item.Builder()
                            .id(rs.getString("item_id"))
                            .title(rs.getString("name"))
                            .location(rs.getString("address"))
                            .companyLogo(rs.getString("image_url"))
                            .url(rs.getString("url"))
                            .keywords(getKeywords(itemId))
                            .favorite(true)
                            .build());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favoriteItems;
    }

    public Set<String> getKeywords(String itemId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return null;
        }
        Set<String> keywords = new HashSet<>();
        String sql = "SELECT keyword from keywords WHERE item_id = ? ";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, itemId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String keyword = rs.getString("keyword");
                keywords.add(keyword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keywords;

    }

    public String getFullname(String userId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return "";
        }
        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";

        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return name;
    }

    public boolean verifyLogin(String userId, String password) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return false;
        }
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean addUser(String userId, String password, String firstname, String lastname) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return false;
        }

        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            statement.setString(3, firstname);
            statement.setString(4, lastname);
            //几条数据受影响，只能是1，更新的行数是1 row，1代表update 数量， 改了几行，就会return几
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }
}

