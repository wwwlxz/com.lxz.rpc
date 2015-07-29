package com.lxz.rpc.test;

import java.io.IOException;

public class RpcProvider {
	/*
	 * 暴露服务
	 */
	public static void main(String[] args) throws IOException{
		HelloService service = new HelloServiceImpl();
		RpcFramework.export(service, 1234);//导出整个接口
	}
}
