package com.jng.callables;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.Callable;

import com.jng.router.RouterState;
import com.jng.utils.BufferUtils;
import com.jng.utils.ChecksumUtils;

// TODO make read functional
public class CallableFactory {

	public RouterState _routerStateRef;

	public void setRouterStateRef(RouterState _routerStateRef) {
		this._routerStateRef = _routerStateRef;
	}

	public IConnectCallable generateBrokerConenctHandler()
	{
		class ConnectionHandler implements IConnectCallable {
			SocketChannel newClient = null;
	
			@Override
			// Method of this Class
			public void setNewClient(SocketChannel _newClient)
			{
				newClient = _newClient;
			}
	
			public Integer call() throws Exception
			{
				int randomId = new Random().nextInt((1000 - 0) + 1);
	
				// assign id add socket to map
				_routerStateRef.getBrokerMap().put(randomId, newClient);
				_routerStateRef.getRevBrokerMap().put(newClient, randomId);

				// append to write list
				String ackMsg = "Broker server accepted connection, your id is " + randomId + "\n";
				_routerStateRef.getPendingToWriteBroker().put(newClient, ackMsg.getBytes(StandardCharsets.UTF_8));
	
				return 0;
			}
		}
		return new ConnectionHandler();
	}

	public IReadCallable generateBrokerReadHandler()
	{
		class ReadHandler implements IReadCallable {

			String readStr = "";
			SocketChannel clientsock = null;
			Selector selector = null;
			
			@Override
			public void setClientSock(SocketChannel clientsock)
			{
				this.clientsock = clientsock;
			}
	
			@Override
			public void setReadStr(String str)
			{
				readStr = str;
			}

			@Override
			public void setSelector(Selector selector)
			{
				this.selector = selector;
			}
	
			// Method of this Class
			public Integer call() throws Exception
			{
				// parse and process...
				
				ChecksumUtils csU = new ChecksumUtils();
				BufferUtils bU = new BufferUtils();

				// if checksum not valid, return error to client
				if (!csU.validateFIXChecksum(readStr))
				{
					String errMsg =
					_routerStateRef.getRevBrokerMap().get(clientsock) +
					"|ERROR=Invalid checksum|"
					;
					int checksumGenerated = csU.getFIXChecksum(errMsg);
					errMsg += ("10="+checksumGenerated);

					_routerStateRef.getPendingToWriteBroker().put(
						clientsock,
						bU.replacePipeWithSOH(bU.strToBytes(errMsg)));
					// register client socket for write
					clientsock.register(selector, SelectionKey.OP_WRITE);
					return 0;
				}

				// get the transaction object

				// garenteed a business transaction, find the market in lookup

				// if not found, return error to client

				// if found, save to db

				// send write to marketsocket

				// if all is successful, set as done

				// should restore if error
				System.out.println(csU.validateFIXChecksum(readStr));
				_routerStateRef.getPendingToWriteBroker().put(
						clientsock,
						bU.replacePipeWithSOH(bU.strToBytes("hello|gay")));

				// register client socket for write
				clientsock.register(selector, SelectionKey.OP_WRITE);
				return 0;
			}
		}

		return new ReadHandler();
	}

	public IWriteCallable generateBrokerWriteHandler()
	{
		class WriteHandler implements IWriteCallable {
			SocketChannel clientSock = null;
			String writeStr = "";
			Selector selector = null;
	
			@Override
			// Method of this Class
			public void setClientSock(SocketChannel _newClient)
			{
				clientSock = _newClient;
			}
	
			@Override
			public void setWriteStr(String writestr)
			{
				writeStr = writestr;
			}

			@Override
			public void setSelector(Selector _selector)
			{
				selector = _selector;	
			}
	
			public Integer call()
			{
				// get pending write for socket
				byte[] message = _routerStateRef.getPendingToWriteBroker().get(clientSock);
	
				if (message == null)
					return 1;
				
				// remove pending message
				_routerStateRef.getPendingToWriteBroker().remove(clientSock);
	
				try {
					// send message to socket
					clientSock.write(ByteBuffer.wrap(message));

					// register client socket to read again
					clientSock.register(selector, SelectionKey.OP_READ);
				} catch (Exception e) {
					System.out.println("failover....");
				}
				
	
				return 0;
			}
		}
		return new WriteHandler();
	}

	public Callable<Integer> createDefaultCallable()
	{
		return new Callable<Integer>() {
			public Integer call() throws Exception
			{
				System.out.println("Default callable.");
				return 0;
			}
		};
	}

	public IConnectCallable createDefaultConenctCallable()
	{		
		class ConnectCallable implements IConnectCallable {
			SocketChannel newClient = null;

			public void setNewClient(SocketChannel _newClient)
			{
				newClient = _newClient;
			}

			public Integer call() throws Exception
			{
				System.out.println("Default connect callable");
				return 1;
			}
		}

		return new ConnectCallable();
	}

	public IReadCallable createDefaultReadCallable()
	{
		class ReadCallable implements IReadCallable {
			String readStr = "";
			SocketChannel clientsock = null;
			Selector selector = null;

			public void setReadStr(String str) {
				readStr = str;
			}
			public void setClientSock(SocketChannel clientsock)
			{
				this.clientsock = clientsock;
			}

			public void setSelector(Selector selector) {
				this.selector = selector;
			}

			public Integer call() throws Exception
			{
				System.out.println("Default read callable");
				return 1;
			}
		}

		return new ReadCallable();
	}

	public IWriteCallable createDefaultWriteCallable()
	{
		class WriteCallable implements IWriteCallable {
			String writeStr = "";
			SocketChannel clientsock = null;
			Selector selector = null;

			public void setWriteStr(String str) {
				writeStr = str;
			}
			public void setClientSock(SocketChannel clientsock)
			{
				this.clientsock = clientsock;
			}
			public void setSelector(Selector selector)
			{
				this.selector = selector;
			}

			public Integer call() throws Exception
			{
				System.out.println("Default read callable");
				return 1;
			}
		}

		return new WriteCallable();
	}
}
