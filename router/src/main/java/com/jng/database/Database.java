package com.jng.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
	private Connection _connection;
	private Statement _statement;

	public Database()
	{
		try {
			Class.forName("org.sqlite.JDBC");
			this._connection = DriverManager.getConnection("jdbc:sqlite:com.jng.db");
			this._statement = _connection.createStatement();
		} catch (Exception e) {
			System.out.println("Cant connect to db: ");
			System.out.println(e);
			System.exit(1);
		}
	}
}
