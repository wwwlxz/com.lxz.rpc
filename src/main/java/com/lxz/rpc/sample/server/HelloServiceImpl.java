package com.lxz.rpc.sample.server;

import com.lxz.rpc.sample.client.HelloService;
import com.lxz.rpc.sample.client.Person;
import com.lxz.rpc.server.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService{

	public String hello(String name) {
		return "Hello! " + name;
	}

	public String hello(Person person) {
		return "Hello! " + person.getFirstName() + " " + person.getLastName();
	}
	
}
