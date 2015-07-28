package com.lxz.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import com.lxz.rpc.commen.RpcRequest;
import com.lxz.rpc.commen.RpcResponse;

/*
 * RPC 代理（用于创建RPC服务代理）
 */
public class RpcProxy {
	private String serverAddress;
	
	public RpcProxy(String serverAddress){
		this.serverAddress = serverAddress;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass){//创建服务
		return (T) Proxy.newProxyInstance(
				interfaceClass.getClassLoader(), 
				new Class<?>[]{interfaceClass}, 
				new InvocationHandler(){
					@Override
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						RpcRequest request = new RpcRequest();
						request.setRequestId(UUID.randomUUID().toString());
						request.setClassName(method.getDeclaringClass().getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);
						
						String[] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						
						RpcClient client = new RpcClient(host, port);
						RpcResponse response = client.send(request);
						
						if(response.isError()){
							throw response.getError();
						}else{
							return response.getResult();
						}
					}
				});
	}
}
