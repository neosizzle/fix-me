package com.jng.callables;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public interface IConnectCallable extends Callable<Integer>{
	
	SocketChannel newClient = null;
	Selector selector = null;

	public void setNewClient(SocketChannel newClient);
	public void setSelector(Selector selector);

	public Integer call() throws Exception;
	
}
