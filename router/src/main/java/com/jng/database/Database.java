package com.jng.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jng.transactions.Transaction;

public class Database {
	private Connection _connection;
	private Statement _statement;

	// TODO implement model methods

	// gets max id from table
	private int _getMaxId(String table)
	{
		String query = "SELECT MAX(id) AS LAST FROM " + table;
		try {
			ResultSet rs = this._statement.executeQuery(query);
			return rs.getInt("last");
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
		return -1;
	}
	

	private void _initTables()
	{
		// create transactions table
		String query = "CREATE TABLE IF NOT EXISTS TRANSACTIONS " +
		"(id			INT			AUTO_INCREMENT PRIMARY KEY     	NOT NULL ," +
		" to_id  INT  NOT NULL, " +
		" from_id  INT  NOT NULL, " + 
		" rawMsg VARCHAR(100) NOT NULL, "+
		" isResponse  INT  NOT NULL, " + 
		" confirmed  INT  NOT NULL, " + 
		" created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL)" ; 

		try {
			this._statement.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				_connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			System.exit(1);
		}
	}

	public int addTransaction(Transaction transaction)
	{
		String query = "INSERT INTO TRANSACTIONS (id, to_id, from_id, rawMsg, isResponse, confirmed) VALUES (?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement stmt = this._connection.prepareStatement(query);
			int id = _getMaxId("TRANSACTIONS") + 1;


			stmt.setInt(1, id);
			stmt.setInt(2, transaction.getToId());
			stmt.setInt(3, transaction.getFromId());
			stmt.setString(4, transaction.getRawMsg());
			stmt.setInt(5, transaction.getIsResponse() ? 1 : 0);
			stmt.setInt(6, 0);

			stmt.executeUpdate();
			return id;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return -1;
	}

	public Database()
	{
		try {
			Class.forName("org.sqlite.JDBC");
			this._connection = DriverManager.getConnection("jdbc:sqlite:com.jng.db");
			this._statement = _connection.createStatement();
			_initTables();
		} catch (Exception e) {
			System.out.println("Cant connect to db: ");
			System.out.println(e);
			System.exit(1);
		}
	}
}
