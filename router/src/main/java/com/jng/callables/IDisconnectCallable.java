package com.jng.callables;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public interface IDisconnectCallable extends Callable<Integer>{
	SocketChannel clientToDc = null;

	public void setClientToDc(SocketChannel clientToDc);

	public Integer call() throws Exception;
}
