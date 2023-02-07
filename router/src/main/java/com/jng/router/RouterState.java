package com.jng.router;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jng.transactions.Transaction;

public class RouterState {
	private Map<Integer, SocketChannel> _marketMap;
	private Map<Integer, SocketChannel> _brokerMap;
	private ArrayList<Transaction> _pendingTransactions;

	public Map<Integer, SocketChannel> getBrokerMap() {
		return _brokerMap;
	}
	public void setBrokerMap(Map<Integer, SocketChannel> _brokerMap) {
		this._brokerMap = _brokerMap;
	}
	public Map<Integer, SocketChannel> getMarketMap() {
		return _marketMap;
	}
	public void setMarketMap(Map<Integer, SocketChannel> _marketMap) {
		this._marketMap = _marketMap;
	}
	public ArrayList<Transaction> getPendingTransactions() {
		return _pendingTransactions;
	}
	public void setPendingTransactions(ArrayList<Transaction> _pendingTransactions) {
		this._pendingTransactions = _pendingTransactions;
	}

	public RouterState()
	{
		_marketMap = new HashMap<Integer, SocketChannel>();
		_brokerMap = new HashMap<Integer, SocketChannel>();
		_pendingTransactions = new ArrayList<Transaction>();
	}
}
