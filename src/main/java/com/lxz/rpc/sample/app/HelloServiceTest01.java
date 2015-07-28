package com.lxz.rpc.sample.app;

import com.lxz.rpc.client.RpcProxy;
import com.lxz.rpc.sample.client.HelloService;

public class HelloServiceTest01 {
	public static void main(String[] args){
		RpcProxy rpcProxy = new RpcProxy("127.0.0.1:1234");
		HelloService helloService = rpcProxy.create(HelloService.class);
		String result = helloService.hello("world");
		System.out.println(result);
	}
}
