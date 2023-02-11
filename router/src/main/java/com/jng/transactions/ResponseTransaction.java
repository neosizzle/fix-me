package com.jng.transactions;

public class ResponseTransaction extends Transaction {
	private String _status;

	public String getStatus() {
		return _status;
	}
	public void setStatus(String _status) {
		this._status = _status;
	}

	ResponseTransaction(
		String status,
		int toId,
		int fromId,
		String rawMsg,
		int checksum
	)
	{
		super(toId, fromId, rawMsg, true, false, checksum);
		this._status = status;

	}
}
