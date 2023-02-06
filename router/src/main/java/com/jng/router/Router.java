package com.jng.router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Callable;

import com.jng.database.Database;
import com.jng.networkServer.NetworkServer;

public class Router {
	private Database _db;
	private NetworkServer _ns;

	private Callable<Integer> test;
	int state;

	/**Setup- connect to db and init sockets */
	public void setup()
	{
		try {
			_db = new Database();
			_ns = new NetworkServer("127.0.0.1", 5000);

			state = 0;
			Callable<Integer> HandleStuff = new Callable<Integer>() {
				// Method of this Class
				public Integer call() throws Exception
				{
					state++;
					System.out.println("curr state: " + state);
					return 0;
				}
			};

			_ns.setHandleConnect(HandleStuff);
			_restore();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/** Restore all pending transactions after startup */
	private void _restore()
	{
		System.out.println("Router._restore()");
	}

	/**Commences listening */
	public void run()
	{	
		System.out.println("Router.run()");
		try {
			_ns.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Router()
	{
		_db = null;
		_ns = null;
	}
}
