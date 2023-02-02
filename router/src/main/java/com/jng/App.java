package com.jng;

import java.sql.DriverManager;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
			Class.forName("org.sqlite.JDBC");
			DriverManager.getConnection("jdbc:sqlite:com.jng.db");
            System.out.println("conencted");
		} catch (Exception e) {
			System.out.println("Cant connect to db: ");
			System.out.println(e);
			System.exit(1);
		}
    }
}
