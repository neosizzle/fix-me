package com.jng.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.jng.transactions.Transaction;

public class Database {
	private Connection _connection;
	private Statement _statement;


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

	private Transaction _getTransactionFromRs(ResultSet rs)
	{
		try {
			int id = rs.getInt("id");
			int toId = rs.getInt("toId");
			int fromId = rs.getInt("fromId");
			String rawMsg = rs.getString("rawMsg");
			boolean isResponse = rs.getInt("isResponse") != 0;
			boolean isConfirmed = rs.getInt("isConfirmed") != 0;
			int checksum = rs.getInt("checksum");
			Transaction res = new Transaction(toId, fromId, rawMsg, isResponse, isConfirmed, checksum);
			res.setId(id);
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	// might not need
	// get all pending transactions from db
	public ArrayList<Transaction> getPendingTransactions() throws SQLException
	{
		ArrayList<Transaction> res = new ArrayList<Transaction>();
		String query = "SELECT * FROM TRANSACTIONS WHERE isConfirmed = 0";

		ResultSet rs = _statement.executeQuery(query);
		while (rs.next()) {
			res.add(_getTransactionFromRs(rs));
		}

		return res;
	}
	
	// add transaction to db
	public int addTransactionToDb(Transaction transaction) throws Exception
	{
		String query = "INSERT INTO TRANSACTIONS (id, to_id, from_id, rawMsg, isResponse, isConfirmed, checksum) VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement stmt = this._connection.prepareStatement(query);
		int id = _getMaxId("TRANSACTIONS") + 1;
		stmt.setInt(1, id);
		stmt.setInt(2, transaction.getToId());
		stmt.setInt(3, transaction.getFromId());
		stmt.setString(4, transaction.getRawMsg());
		stmt.setInt(5, transaction.getIsResponse() ? 1 : 0);
		stmt.setInt(6, transaction.getIsConfirmed()? 1 : 0);
		stmt.setInt(7, transaction.getChecksum());

		stmt.executeUpdate();

		return id;
	}

	// mark a transaction as complete
	public void completeTransaction(Transaction transaction) throws SQLException
	{
		int id = transaction.getId();
		if (id == -1) return;

		String query = "UPDATE TRANSACTIONS SET isConfirmed = 1 WHERE id = " + id;
		_statement.executeUpdate(query);

	}

	// delete a transaction
	public void deleteTransaction(Transaction transaction) throws SQLException
	{
		int id = transaction.getId();
		if (id == -1) return;

		String query = "DELETE FROM TRANSACTIONS WHERE id = " + id;
		_statement.executeUpdate(query);
	}

	// get transaction by id
	public Transaction getTransactionById(int id) throws SQLException
	{
		Transaction res;
		String query = "SELECT * FROM TRANSACTIONS WHERE isConfirmed = 0";

		ResultSet rs = _statement.executeQuery(query);
		while (rs.next()) {
			res = _getTransactionFromRs(rs);
			return res;
		}

		return null;
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
		" isConfirmed  INT  NOT NULL, " + 
		" checksum  INT  NOT NULL, " + 
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
