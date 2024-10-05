package com.caurix.distributorauto.web;

public class WebTaskDelegate {
	public void synchronousPostExecute(String sr) {

	}

	public void synchronousPreExecute() {

	}

	public void asynchronousPostExecute(String sr) {

	}

	public void asynchronousPreExecute() {

	}
	
	public void onError(Exception e){
		throw new RuntimeException(e);
	}
}