package com.jng.router;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jng.database.Database;
import com.jng.transactions.Transaction;

public class RouterState {
	private Map<Integer, SocketChannel> _marketMap;
	private Map<SocketChannel, Integer> _revMarketMap;
	private Map<SocketChannel, byte[]>  _pendingToWriteMarket;
	private Map<Integer, SocketChannel> _brokerMap;
	private Map<SocketChannel, Integer> _revBrokerMap;
	private Map<SocketChannel, byte[]>  _pendingToWriteBroker;
	private ArrayList<Transaction> _pendingTransactions;
	private Database _db;

	public Map<Integer, SocketChannel> getBrokerMap() {
		return _brokerMap;
	}
	public void setBrokerMap(Map<Integer, SocketChannel> _brokerMap) {
		this._brokerMap = _brokerMap;
	}
	public Map<SocketChannel, Integer> getRevBrokerMap() {
		return _revBrokerMap;
	}
	public void setRevBrokerMap(Map<SocketChannel, Integer> _revBrokerMap) {
		this._revBrokerMap = _revBrokerMap;
	}
	public Map<SocketChannel, Integer> getRevMarketMap() {
		return _revMarketMap;
	}
	public void setMevMarketMap(Map<SocketChannel, Integer> _revMarketMap) {
		this._revMarketMap = _revMarketMap;
	}
	public Map<SocketChannel, byte[]> getPendingToWriteBroker() {
		return _pendingToWriteBroker;
	}
	public void setPendingToWriteBroker(Map<SocketChannel, byte[]> _pendingToWriteBroker) {
		this._pendingToWriteBroker = _pendingToWriteBroker;
	}
	public Map<SocketChannel, byte[]> getPendingToWriteMarket() {
		return _pendingToWriteMarket;
	}
	public void setPendingToWriteMarket(Map<SocketChannel, byte[]> _pendingToWriteMarket) {
		this._pendingToWriteMarket = _pendingToWriteMarket;
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
	public Database getDb() {
		return _db;
	}
	public void setDb(Database _db) {
		this._db = _db;
	}

	public RouterState()
	{
		_marketMap = new HashMap<Integer, SocketChannel>();
		_brokerMap = new HashMap<Integer, SocketChannel>();
		_revMarketMap = new HashMap<SocketChannel, Integer>();
		_revBrokerMap = new HashMap<SocketChannel, Integer>();
		_pendingTransactions = new ArrayList<Transaction>();
		_pendingToWriteBroker = new HashMap<SocketChannel, byte[]>();
		_pendingToWriteMarket = new HashMap<SocketChannel, byte[]>();
	}
}
