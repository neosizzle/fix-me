package com.jng.router;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jng.callables.CallableFactory;
import com.jng.callables.IConnectCallable;
import com.jng.callables.IDisconnectCallable;
import com.jng.callables.IReadCallable;
import com.jng.callables.IWriteCallable;
import com.jng.database.Database;
import com.jng.networkServer.NetworkServer;

public class Router {
	private Database _db;
	private ExecutorService _executorService = Executors.newFixedThreadPool(2);  
	private RouterState _routerState;
	private NetworkServer _brokerServer;
	private NetworkServer _marketServer;

	private CallableFactory _callableFactory;

	/** Handlers Broker */
	private IConnectCallable _handleConnectionBroker;
	private IReadCallable _handleReadBroker;
	private IWriteCallable _handleWriteBroker;
	private IDisconnectCallable _handleDisconnectBroker;

	/** Handlers Market */
	private IConnectCallable _handleConnectionMarket;
	private IWriteCallable _handleWriteMarket;
	private IReadCallable _handleReadMarket;
	private IDisconnectCallable _handleDisconnectMarket;

	private void _generateMarketHandlers()
	{
		_handleConnectionMarket = _callableFactory.generateMarketConnectHandler();
		_handleWriteMarket = _callableFactory.generateMarketWriteHandler();
		_handleReadMarket = _callableFactory.generateMarketReadHandler();
		_handleDisconnectMarket = _callableFactory.generateMarketDisconnectHandler();
	}

	private void _generateBrokerHandlers()
	{
		_handleConnectionBroker = _callableFactory.generateBrokerConnectHandler();
		_handleReadBroker = _callableFactory.generateBrokerReadHandler();
		_handleWriteBroker = _callableFactory.generateBrokerWriteHandler();
		_handleDisconnectBroker = _callableFactory.generateBrokerDisconnectHandler();
	}

	/**Setup- connect to db and init sockets */
	public void setup()
	{
		try {
			_db = new Database();
			_brokerServer = new NetworkServer("127.0.0.1", 5000, _routerState);
			_marketServer = new NetworkServer("127.0.0.1", 5001, _routerState);

			_routerState.setDb(_db);
			_callableFactory.setRouterStateRef(_routerState);

			_generateBrokerHandlers();
			_brokerServer.setHandleConnect(_handleConnectionBroker);
			_brokerServer.setHandleRead(_handleReadBroker);
			_brokerServer.setHandleWrite(_handleWriteBroker);
			_brokerServer.setHandleDisconnect(_handleDisconnectBroker);

			_generateMarketHandlers();
			_marketServer.setHandleConnect(_handleConnectionMarket);
			_marketServer.setHandleRead(_handleReadMarket);
			_marketServer.setHandleWrite(_handleWriteMarket);
			_marketServer.setHandleDisconnect(_handleDisconnectMarket);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

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
		_callableFactory = new CallableFactory();
	}
}
