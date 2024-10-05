package com.caurix.distributorauto.bean;

public class TransactionRecord {
	private String id;
	private String transactionId;
	private String amount;
	private String status;
	private String clientNumber;
	private String sdNumber;
	private String sdName;
	private String transactionType;

	public TransactionRecord() {

	}

	public TransactionRecord(String id, String transactionId, String amount,
			String status, String clientNumber, String sdNumber, String sdName,
			String transactionType) {
		this();
		this.id = id;
		this.transactionId = transactionId;
		this.amount = amount;
		this.status = status;
		this.clientNumber = clientNumber;
		this.sdNumber = sdNumber;
		this.sdName = sdName;
		this.transactionType = transactionType;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getClientNumber() {
		return clientNumber;
	}

	public void setClientNumber(String clientNumber) {
		this.clientNumber = clientNumber;
	}

	public String getSdNumber() {
		return sdNumber;
	}

	public void setSdNumber(String sdNumber) {
		this.sdNumber = sdNumber;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSdName() {
		return sdName;
	}

	public void setSdName(String sdName) {
		this.sdName = sdName;
	}
}
