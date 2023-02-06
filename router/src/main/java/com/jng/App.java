package com.jng;

import java.sql.DriverManager;

import com.jng.router.Router;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Router router = new Router();

		router.setup();
		router.run();
    }
}
