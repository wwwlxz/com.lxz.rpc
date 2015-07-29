package com.lxz.rpc.test;

public class HelloServiceImpl implements HelloService{
	/*
	 * 实现服务
	 */
	@Override
	public String hello(String name) {
		return "Hello " + name;
	}

}
