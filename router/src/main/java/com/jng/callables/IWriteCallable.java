package com.jng.callables;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public interface IWriteCallable extends Callable<Integer>{
	String writeStr = "";
	SocketChannel clientsock = null;
	Selector selector = null;

	public void setWriteStr(String str);
	public void setClientSock(SocketChannel clientsock);
	public void setSelector(Selector selector);
	
	public Integer call() throws Exception;
}
