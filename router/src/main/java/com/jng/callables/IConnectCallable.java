package com.jng.callables;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public interface IConnectCallable extends Callable<Integer>{
	
	SocketChannel newClient = null;

	public void setNewClient(SocketChannel newClient);

	public Integer call() throws Exception;
	
}
