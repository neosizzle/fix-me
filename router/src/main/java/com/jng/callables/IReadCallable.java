package com.jng.callables;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public interface IReadCallable extends Callable<Integer>{
	String readStr = "";
	SocketChannel clientsock = null;
	Selector selector = null;

	public void setReadStr(String str);
	public void setClientSock(SocketChannel clientsock);
	public void setSelector(Selector selector);
	
	public Integer call() throws Exception;
}
