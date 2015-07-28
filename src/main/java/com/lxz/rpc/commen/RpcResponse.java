package com.lxz.rpc.commen;

/*
 * 封装RPC响应
 */
public class RpcResponse {
	private String requestId;//请求ID
	private Throwable error;//错误
	private Object result;//结果
	
	public boolean isError(){
		return error != null;
	}
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public Throwable getError() {
		return error;
	}
	
	public void setError(Throwable error) {
		this.error = error;
	}
	
	public Object getResult() {
		return result;
	}
	
	public void setResult(Object result) {
		this.result = result;
	}
	
	public static void main(String[] args){
		RpcResponse response = new RpcResponse();
		System.out.println(response.isError());
		System.out.println(response.getError());
	}
}
