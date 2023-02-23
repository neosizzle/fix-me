package com.jng.callables;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.jng.router.RouterLogger;
import com.jng.router.RouterState;
import com.jng.transactions.BusinessTransaction;
import com.jng.transactions.ResponseTransaction;
import com.jng.transactions.Transaction;
import com.jng.utils.BufferUtils;
import com.jng.utils.ChecksumUtils;
import com.jng.utils.HandlerUtils;

public class CallableFactory {

	public RouterState _routerStateRef;
	public RouterLogger _routerLogger = new RouterLogger();

	public void setRouterStateRef(RouterState _routerStateRef) {
		this._routerStateRef = _routerStateRef;
	}

	private String _generateRestoreMsg(Transaction transactionToRes, Boolean isBroker) throws Exception
	{
		BufferUtils bU = new BufferUtils();
		ChecksumUtils cU = new ChecksumUtils();
		char soh = (char) 1;
		String res;

		res = "";
		if (isBroker)
		{
			byte[] rawMsgBytes = bU.strToBytes(transactionToRes.getRawMsg());
			BusinessTransaction businessTransaction = new BusinessTransaction(rawMsgBytes);
		
			res += businessTransaction.getFromId() + String.valueOf(soh);
			res += "restoreAmount=" + businessTransaction.getPrice() + String.valueOf(soh);
			
			res += "action=" + (businessTransaction.getIsBuy() ? "buy" : "sell") + String.valueOf(soh); 

			res += "instrument=" + businessTransaction.getInstrument() + String.valueOf(soh); 

			int checksum = cU.getFIXChecksum(res, false);
			res += "10=" + checksum + String.valueOf(soh);
		}
		else {
			byte[] rawMsgBytes = bU.strToBytes(transactionToRes.getRawMsg());
			ResponseTransaction responseTransaction = new ResponseTransaction(rawMsgBytes);

			res += responseTransaction.getFromId() + String.valueOf(soh);
			res += "responseInstrument=" + responseTransaction.getInstrument() + String.valueOf(soh);
			res += "responseAction=" + responseTransaction.getResponse() + String.valueOf(soh);
			int checksum = cU.getFIXChecksum(res, false);
			res += "10=" + checksum + String.valueOf(soh);
		}
		return res;
	}

	public IConnectCallable generateBrokerConnectHandler()
	{
		class ConnectionHandler implements IConnectCallable {
			SocketChannel newClient = null;
			Selector selector = null;
	
			@Override
			// Method of this Class
			public void setNewClient(SocketChannel _newClient)
			{
				newClient = _newClient;
			}

			@Override
			// Method of this Class
			public void setSelector(Selector sel)
			{
				selector = sel;

				// set to routerstate selector if its not present
				if (!_routerStateRef
				.getBrokerSelectors()
				.keySet()
				.contains(newClient))
				{
					_routerStateRef
					.getBrokerSelectors()
					.put(newClient, selector);
				}
				
			}
	
			public Integer call() throws Exception
			{
				int randomId = new Random().nextInt((1000 - 0) + 1);
	
				_routerLogger.logDebug("New broker connected, giving id " + randomId);

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
			HandlerUtils hU = new HandlerUtils();
			Selector brokerSelector;
			
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

	
			// Method of this Class
			public Integer call()
			{				
				ChecksumUtils csU = new ChecksumUtils();
				BufferUtils bU = new BufferUtils();

				brokerSelector = _routerStateRef.getBrokerSelectors().get(clientsock);

				// if checksum not valid, return error to client
				// TEST OK
				if (!csU.validateFIXChecksum(readStr))
				{
					// generat eerr msg
					String errMsg = hU.generateErrMsg(
					"|ERROR=Invalid checksum|",
					_routerStateRef.getRevBrokerMap().get(clientsock),
					true);

					// put string to client
					hU.putStringToClient(
					_routerStateRef.getPendingToWriteBroker(),
					clientsock,
					errMsg);
					// register client socket for write
					try {
						clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
					} catch (Exception e) {
						e.printStackTrace();
						_routerLogger.logErr(e.getMessage());
					}
					return 0;
				}

				// check for invalid character - |
				// TEST OK
				for (int i = 0; i < readStr.length(); i++) {
					if (readStr.charAt(i) == '|')
					{
						String errMsg = hU.generateErrMsg(
						"|ERROR=Invalid Character|",
						_routerStateRef.getRevBrokerMap().get(clientsock),
						true);

						// put string to client
						hU.putStringToClient(
							_routerStateRef.getPendingToWriteBroker(),
							clientsock,
							errMsg);

							// register client socket for write
							try {
								clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
							} catch (Exception e) {
								e.printStackTrace();
								_routerLogger.logErr(e.getMessage());
							}
						return 0;
					}
				}

				BusinessTransaction newTrans;

				// TEST OK
				try {
					// get the transaction object, return error if parse failed
					newTrans = new BusinessTransaction(bU.strToBytes(readStr));
				} catch (Exception e) {
					String errMsg = hU.generateErrMsg(
						"|ERROR=" + e.getMessage() + "|",
						_routerStateRef.getRevBrokerMap().get(clientsock),
						true);

						// put string to client
						hU.putStringToClient(
							_routerStateRef.getPendingToWriteBroker(),
							clientsock,
							errMsg);
					// register client socket for write
					try {
						clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
					} catch (Exception e2) {
						e2.printStackTrace();
						_routerLogger.logErr(e2.getMessage());
					}
					return 0;
				}

				// broker id not recognized, should throw err
				if (!_routerStateRef.getBrokerMap().keySet().contains(newTrans.getFromId()))
				{
					String errMsg = hU.generateErrMsg(
						"|ERROR=Broker not recognized|",
					_routerStateRef.getRevBrokerMap().get(clientsock),
					true);

					// put string to client
					hU.putStringToClient(
						_routerStateRef.getPendingToWriteBroker(),
						clientsock,
						errMsg);

					// register client socket for write
					try {
						clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
					} catch (Exception e) {
						e.printStackTrace();
						_routerLogger.logErr(e.getMessage());
					}
					return 0;
				}

				// garenteed a business transaction, find the market in lookup
				SocketChannel marketSocket = _routerStateRef.getMarketMap().get(newTrans.getMarket());

				// if not found, return error to client
				// TEST OK
				if (marketSocket == null)
				{
						String errMsg = hU.generateErrMsg(
						"|ERROR=Market not found|",
						_routerStateRef.getRevBrokerMap().get(clientsock),
						true);

						// put string to client
						hU.putStringToClient(
							_routerStateRef.getPendingToWriteBroker(),
							clientsock,
							errMsg);
						// register client socket for write
						try {
							clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
						} catch (Exception e) {
							e.printStackTrace();
							_routerLogger.logErr(e.getMessage());
						}
						return 0;
				}

				// if found, save to db
				int transId = -1;
				try {
					transId = _routerStateRef.getDb().addTransactionToDb(newTrans);
				} catch (Exception e) {
					e.printStackTrace();
					// commence restore
					try {
						String restoreMsg = _generateRestoreMsg(newTrans, true);
						_routerStateRef.getPendingToWriteBroker().put(
							clientsock,
							bU.replacePipeWithSOH(bU.strToBytes(restoreMsg)));
						clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
						return 1;
					} catch (Exception e2) {
						e2.printStackTrace();
						_routerLogger.logErr(e2.getMessage());
						return 1;
					}
				}

				try {
					// send write to marketsocket with transaction id
					Selector marketSelector = _routerStateRef.getMarketSelectors().get(marketSocket);
					newTrans.setId(transId);

					String readStrTokens[] = readStr.split(String.valueOf((char) 1), -1);
					readStrTokens[readStrTokens.length - 2] = "transId=" + transId;
					String msgToMarket = String.join(String.valueOf((char) 1), readStrTokens); 
					int checksum = csU.getFIXChecksum(msgToMarket, false);
					msgToMarket += "10="+checksum + String.valueOf((char)1);
					_routerStateRef
					.getPendingToWriteMarket()
					.put(marketSocket, bU.strToBytes(msgToMarket));
					marketSocket.register(marketSelector, SelectionKey.OP_WRITE);

					// if all is successful, set as done
					// _routerStateRef.getDb().completeTransaction(newTrans);

				} catch (Exception e) {
					e.printStackTrace();
					// commence restore
					try {
						String restoreMsg = _generateRestoreMsg(newTrans, true);
						_routerStateRef.getPendingToWriteBroker().put(
							clientsock,
							bU.replacePipeWithSOH(bU.strToBytes(restoreMsg)));
						clientsock.register(brokerSelector, SelectionKey.OP_WRITE);
					} catch (Exception e2) {
						e2.printStackTrace();
						_routerLogger.logErr(e2.getMessage());
					}
				}
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
					// write fail
					e.printStackTrace();
				}
				
	
				return 0;
			}
		}
		return new WriteHandler();
	}

	public IDisconnectCallable generateBrokerDisconnectHandler()
	{
		class DisconnectHandler implements IDisconnectCallable {
			SocketChannel clientToDc = null;

			@Override
			public void setClientToDc(SocketChannel clientToDc) {
				this.clientToDc = clientToDc;
			}
			@Override
			public Integer call() throws Exception {
				int brokerId = _routerStateRef.getRevBrokerMap().get(clientToDc);

				_routerLogger.logDebug("Broker " + brokerId + " disconnected from router.");
				// remove from socketmap
				_routerStateRef.getBrokerMap().remove(brokerId);

				// remove from rev map
				_routerStateRef.getRevBrokerMap().remove(clientToDc);

				// remove from selector
				_routerStateRef.getBrokerSelectors().remove(clientToDc);

				// remove from selector keys
				_routerStateRef.getBrokerSelectorKeys().remove(clientToDc);
				return null;
			}
		}

		return new DisconnectHandler();
	}

	public IConnectCallable generateMarketConnectHandler()
	{
		class ConnectionHandler implements IConnectCallable {
			SocketChannel newClient = null;
			Selector selector = null;
	
			@Override
			// Method of this Class
			public void setNewClient(SocketChannel _newClient)
			{
				newClient = _newClient;
			}

			@Override
			// Method of this Class
			public void setSelector(Selector sel)
			{
				selector = sel;

				// set to routerstate selector if its not present
				if (!_routerStateRef
				.getMarketSelectors()
				.keySet()
				.contains(newClient))
				{
					_routerStateRef
					.getMarketSelectors()
					.put(newClient, selector);
				}
				
			}

			public Integer call() throws Exception
			{
				int randomId = new Random().nextInt((1000 - 0) + 1);
	
				_routerLogger.logDebug("New market connected, giving id " + randomId);
				// assign id add socket to map
				_routerStateRef.getMarketMap().put(randomId, newClient);
				_routerStateRef.getRevMarketMap().put(newClient, randomId);

				// append to write list
				String ackMsg = "Market server accepted connection, your id is " + randomId + "\n";
				_routerStateRef.getPendingToWriteMarket().put(newClient, ackMsg.getBytes(StandardCharsets.UTF_8));
	
				return 0;
			}
		}
		return new ConnectionHandler();
	}

	public IReadCallable generateMarketReadHandler()
	{
		class ReadHandler implements IReadCallable {

			String readStr = "";
			SocketChannel clientsock = null;
			Selector marketSelector; // market selector
			HandlerUtils hU = new HandlerUtils();
			
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
			// Method of this Class
			public Integer call()
			{
				ChecksumUtils csU = new ChecksumUtils();
				BufferUtils bU = new BufferUtils();

				marketSelector = _routerStateRef.getMarketSelectors().get(clientsock);

				// if checksum not valid, return error to client
				// TEST OK
				if (!csU.validateFIXChecksum(readStr))
				{
					// generat eerr msg
					String errMsg = hU.generateErrMsg(
					"|ERROR=Invalid checksum|",
					_routerStateRef.getRevMarketMap().get(clientsock),
					true);

					// put string to client
					hU.putStringToClient(
					_routerStateRef.getPendingToWriteMarket(),
					clientsock,
					errMsg);
					// register client socket for write
					try {
						clientsock.register(marketSelector, SelectionKey.OP_WRITE);
					} catch (Exception e) {
						e.printStackTrace();
						_routerLogger.logErr(e.getMessage());
					}
					return 0;
				}

				// check for invalid character - |
				// TEST OK
				for (int i = 0; i < readStr.length(); i++) {
					if (readStr.charAt(i) == '|')
					{
						String errMsg = hU.generateErrMsg(
						"|ERROR=Invalid Character|",
						_routerStateRef.getRevMarketMap().get(clientsock),
						true);

						// put string to client
						hU.putStringToClient(
							_routerStateRef.getPendingToWriteMarket(),
							clientsock,
							errMsg);

							// register client socket for write
							try {
								clientsock.register(marketSelector, SelectionKey.OP_WRITE);
							} catch (Exception e) {
								e.printStackTrace();
								_routerLogger.logErr(e.getMessage());
							}
						return 0;
					}
				}

				// get the response transaction object, return error if parse fails
				ResponseTransaction newTrans;

				try {
					newTrans = new ResponseTransaction(bU.strToBytes(readStr));
				} catch (Exception e) {
					String errMsg = hU.generateErrMsg(
						"|ERROR=" + e.getMessage() + "|",
						_routerStateRef.getRevMarketMap().get(clientsock),
						true);

					// put string to client
					hU.putStringToClient(
						_routerStateRef.getPendingToWriteMarket(),
						clientsock,
						errMsg);
					// register client socket for write
					try {
						clientsock.register(marketSelector, SelectionKey.OP_WRITE);
					} catch (Exception e2) {
						e2.printStackTrace();
						_routerLogger.logErr(e2.getMessage());
					}
					return 0;
					
				}

				// market Id is not recognigzed throw error
				if (!_routerStateRef.getMarketMap().keySet().contains(newTrans.getFromId()))
				{
					String errMsg = hU.generateErrMsg(
						"|ERROR=Market not recognized|",
					_routerStateRef.getRevMarketMap().get(clientsock),
					true);

					// put string to client
					hU.putStringToClient(
						_routerStateRef.getPendingToWriteMarket(),
						clientsock,
						errMsg);

					// register client socket for write
					try {
						clientsock.register(marketSelector, SelectionKey.OP_WRITE);
					} catch (Exception e) {
						e.printStackTrace();
						_routerLogger.logErr(e.getMessage());
					}
					return 0;
				}

				// find the broker in lookup
				SocketChannel brokerSocket = _routerStateRef.getBrokerMap().get(newTrans.getBroker());

				// if not found, return error to client
				// TEST OK
				if (brokerSocket == null)
				{
						String errMsg = hU.generateErrMsg(
						"|ERROR=Broker not found|",
						_routerStateRef.getRevMarketMap().get(clientsock),
						true);

						// put string to client
						hU.putStringToClient(
							_routerStateRef.getPendingToWriteMarket(),
							clientsock,
							errMsg);
						// register client socket for write
						try {
							clientsock.register(marketSelector, SelectionKey.OP_WRITE);
						} catch (Exception e) {
							e.printStackTrace();
							_routerLogger.logErr(e.getMessage());
						}
						return 0;
				}

				// TODO check if transaction exists and is not completed already - DROPPED (i dont wanna look cool)

				// mark as complete for transaction
				try {
					_routerStateRef.getDb().completeTransaction(newTrans);
				} catch (Exception e) {
					e.printStackTrace();
					// commence restore
					try {
						String restoreMsg = _generateRestoreMsg(newTrans, true);
						_routerStateRef.getPendingToWriteMarket().put(
							clientsock,
							bU.replacePipeWithSOH(bU.strToBytes(restoreMsg)));
						clientsock.register(marketSelector, SelectionKey.OP_WRITE);
						return 1;
					} catch (Exception e2) {
						_routerLogger.logErr(e2.getMessage());
						e2.printStackTrace();
						return 1;
					}
				}

				// send response to broker
				try {
					Selector brokerSelector = _routerStateRef.getBrokerSelectors().get(brokerSocket);
					_routerStateRef
					.getPendingToWriteBroker()
					.put(brokerSocket, bU.strToBytes(readStr));
					brokerSocket.register(brokerSelector, SelectionKey.OP_WRITE);
				} catch (Exception e) {
					e.printStackTrace();
					// commence restore
					try {
						String restoreMsg = _generateRestoreMsg(newTrans, true);
						_routerStateRef.getPendingToWriteMarket().put(
							clientsock,
							bU.replacePipeWithSOH(bU.strToBytes(restoreMsg)));
						clientsock.register(marketSelector, SelectionKey.OP_WRITE);
						return 1;
					} catch (Exception e2) {
						e2.printStackTrace();
						_routerLogger.logErr(e2.getMessage());
						return 1;
					}
				}
				return 0;
			}
		}

		return new ReadHandler();
	}

	public IWriteCallable generateMarketWriteHandler()
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
				byte[] message = _routerStateRef.getPendingToWriteMarket().get(clientSock);
	
				if (message == null)
					return 1;
				
				// remove pending message
				_routerStateRef.getPendingToWriteMarket().remove(clientSock);
	
				try {
					// send message to socket
					clientSock.write(ByteBuffer.wrap(message));

					// System.out.println("written writecall.()" + new String(message, "ASCII"));

					// register client socket to read again
					clientSock.register(selector, SelectionKey.OP_READ);

				} catch (Exception e) {
					// write fail
					e.printStackTrace();
				}
				
	
				return 0;
			}
		}
		return new WriteHandler();
	}

	public IDisconnectCallable generateMarketDisconnectHandler()
	{
		class DisconnectHandler implements IDisconnectCallable {
			SocketChannel clientToDc = null;

			@Override
			public void setClientToDc(SocketChannel clientToDc) {
				this.clientToDc = clientToDc;
			}
			@Override
			public Integer call() throws Exception {
				int marketId = _routerStateRef.getRevMarketMap().get(clientToDc);

				_routerLogger.logDebug("Market " + marketId + " disconnected from router.");
				// remove from socketmap
				_routerStateRef.getMarketMap().remove(marketId);

				// remove from rev map
				_routerStateRef.getRevMarketMap().remove(clientToDc);

				// remove from selector
				_routerStateRef.getMarketSelectors().remove(clientToDc);

				// remove from selector keys
				_routerStateRef.getMarketSelectorKeys().remove(clientToDc);
				return null;
			}
		}

		return new DisconnectHandler();
	}
	
}
