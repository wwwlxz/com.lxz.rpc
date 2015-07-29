package com.lxz.rpc.test;


//http://javatar.iteye.com/blog/1123915
public class RpcConsumer {
	/*
	 * 引用服务
	 */
	public static void main(String[] args) throws InterruptedException{
		HelloService service = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
		for(int i = 0; i < Integer.MAX_VALUE; i++){
			String hello = service.hello("World" + i);
			System.out.println(hello);
			Thread.sleep(1000);
		}
	}
}
