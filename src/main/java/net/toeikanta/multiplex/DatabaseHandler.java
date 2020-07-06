package net.toeikanta.multiplex;

import code.nextgen.sqlibrary.database.DatabaseConnection;
import code.nextgen.sqlibrary.database.SQLiteConnection;

import java.io.File;
import java.sql.SQLException;

public class DatabaseHandler {
    //Creating a SQLiteConnection
    DatabaseConnection connection;

    public void connectDB() throws SQLException, ClassNotFoundException {
        this.connection = new SQLiteConnection(new File("my.db"));
        //Opening a DatabaseConnection
        this.connection.openConnection();
    }
}
