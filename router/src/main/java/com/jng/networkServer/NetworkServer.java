package com.jng.networkServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import com.jng.callables.CallableFactory;
import com.jng.router.RouterState;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NetworkServer {

	// network
	private String _host;
	private int _port;
	private ServerSocketChannel _serverChannel;
	private Selector _selector;
	private boolean _isRunning;
	private Map<SocketChannel, byte[]> _pendingToWrite = new HashMap<>();
	private final int TIMEOUT = 500;
	private String _ackMessage;
	private ArrayList<SocketChannel> _connectedClients;
	
	// business logic
	private RouterState _routerState;
	private CallableFactory _callableFactory;
	private Callable<Integer> _handleConnect;
	// TODO implement signal handling - scrapped, no standard API available


	public void setAckMessage(String _ackMessage) {
		this._ackMessage = _ackMessage;
	}

	public void setHandleConnect(Callable<Integer> _handleConnect) {
		this._handleConnect = _handleConnect;
	}

	public void start() throws Exception
	{
		System.out.println("Server starting at " + _host + ":" + _port);
		while (_isRunning) {
			// wait for select to return
			_selector.select(TIMEOUT);

			// get all ready keys (fds)
			Iterator<SelectionKey> keys = _selector.selectedKeys().iterator();

			// iterate through all ready fds
			while (keys.hasNext()) {
				SelectionKey key = keys.next();

				// remvoe fd from set
				keys.remove();

				// check for invalid keys - client close conn
				if (!key.isValid()) continue;

				// check for new connection
				if (key.isAcceptable())
				{
					// accept new connection
					SocketChannel clientSocket = _serverChannel.accept();
					
					// set client socket to non block mode
					clientSocket.configureBlocking(false);

					// send ack message to client
					byte[] arr = _ackMessage.getBytes(StandardCharsets.UTF_8);
					_pendingToWrite.put(clientSocket, arr);

					// register client to fd set for write
					clientSocket.register(_selector, SelectionKey.OP_WRITE);

					// add client socket to connected clients
					_connectedClients.add(clientSocket);

					// run callables
					_handleConnect.call();

					continue ;
				}

				// check for read ready
				if (key.isReadable())
				{
					// get socket from selector
					SocketChannel clientSocket = (SocketChannel) key.channel();
					
					// read data
					ByteBuffer readBuffer = ByteBuffer.allocate(1024);
					readBuffer.clear();

					int read;
					try {
						read = clientSocket.read(readBuffer);
					} catch (IOException e) {
						e.printStackTrace();
						key.cancel();
						clientSocket.close();
						_serverChannel.close();
						return;
					}

					if (read == -1) {
						System.out.println("Client disconnect");
						
						// remove from connected clients
						_connectedClients.remove(clientSocket);

						clientSocket.close();
						key.cancel();
						continue;
					}	

					// process and push data to write pending
					byte[] arr = new byte[read];
					readBuffer.flip();
					readBuffer.get(arr);
					_pendingToWrite.put(clientSocket,arr);

					// register client socket for write
					clientSocket.register(_selector, SelectionKey.OP_WRITE);

					continue ;
				}

				// check for write ready
				if (key.isWritable())
				{
					// get socket
					SocketChannel clientSocket = (SocketChannel) key.channel();

					// get message from hashmap
					byte[] message = _pendingToWrite.get(clientSocket);

					if (message == null)
						continue;
					
					// remove pending message
					_pendingToWrite.remove(clientSocket);

					// send message to socket
					clientSocket.write(ByteBuffer.wrap(message));

					// register client socket to read again
					clientSocket.register(_selector, SelectionKey.OP_READ);

				}
			}
		}
	}

	private void _setup() throws IOException
	{

		// instantiate new server channel
		_serverChannel = ServerSocketChannel.open();

		// fcntl O_NONBLOCK - make sockets throw EAGAIN
		_serverChannel.configureBlocking(false);

		// binding stuff
		_serverChannel.socket().bind(new InetSocketAddress(_host, _port));

		// initialize select
		_selector = Selector.open();

		// add server socket to select as read socket set
		_serverChannel.register(_selector, SelectionKey.OP_ACCEPT);

	}

	public NetworkServer(String host, int port, RouterState routerstate) throws IOException
	{
		_host = host;
		_port = port;
		_isRunning = true;
		_routerState = routerstate;
		_callableFactory = new CallableFactory();
		_handleConnect = _callableFactory.createDefaultCallable();
		_ackMessage = "";
		_connectedClients = new ArrayList<SocketChannel>();
		_setup();
	}
}
