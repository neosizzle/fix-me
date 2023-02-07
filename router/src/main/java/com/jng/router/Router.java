package com.jng.router;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jng.database.Database;
import com.jng.networkServer.NetworkServer;
import com.jng.transactions.BusinessTransaction;

public class Router {
	private Database _db;
	private ExecutorService _executorService = Executors.newFixedThreadPool(2);  
	private RouterState _routerState;
	private NetworkServer _brokerServer;
	private NetworkServer _marketServer;

	/** Handlers */
	private Callable<Integer> _handleConnection;
	int numConnections = 0;

	private void _generateHandlers()
	{
		_handleConnection = new Callable<Integer>() {
			// Method of this Class
			public Integer call() throws Exception
			{
				numConnections++;
				System.out.println("curr numConnections: " + numConnections);
				_db.addTransaction(new BusinessTransaction(
					"instru",
					43.21 ,
					"market", 
					2.34,
					1,
					2,
					"rawnmsg",
					1234));
				return 0;
			}
		};
	}

	/**Setup- connect to db and init sockets */
	public void setup()
	{
		try {
			_db = new Database();
			_brokerServer = new NetworkServer("127.0.0.1", 5000, _routerState);
			_marketServer = new NetworkServer("127.0.0.1", 5001, _routerState);
			_brokerServer.setAckMessage("Broker server accepted connection\n");
			_marketServer.setAckMessage("Market server accepted connection\n");

			_generateHandlers();
			_brokerServer.setHandleConnect(_handleConnection);
			_marketServer.setHandleConnect(_handleConnection);
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
			_executorService.execute(new Runnable() {  
              
				@Override  
				public void run() {   
					try {
						_brokerServer.start();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}  
			});  

			_executorService.execute(new Runnable() {  
              
				@Override  
				public void run() {   
					try {
						_marketServer.start();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}  
			}); 

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Router()
	{
		_db = null;
		_brokerServer = null;
		_routerState = new RouterState();
	}
}
