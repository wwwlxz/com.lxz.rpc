package com.lxz.rpc.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

public class RpcFramework {
	/* 服务端调用
	 * 暴露服务
	 * @param service服务实现
	 * @param port服务端口
	 */
	public static void export(final Object service, int port) throws IOException{
		if(service == null){
			throw new IllegalArgumentException("service instance == null");
		}
		if(port <= 0 || port > 65535){
			throw new IllegalArgumentException("Invalid port " + port);
		}
		System.out.println("Export service " + service.getClass().getName() + 
				" on port " + port);
		ServerSocket server = new ServerSocket(port);
		for(;;){
			try{
				final Socket socket = server.accept();
				new Thread(new Runnable(){
					@Override
					public void run() {
						try{
							try{
								ObjectInputStream input = new ObjectInputStream(socket.getInputStream());//创建一个输入流
								try{
									String methodName = input.readUTF();//获取方法的名字
									Class<?>[] parameterTypes = (Class<?>[])input.readObject();//获取方法的参数类型
									Object[] arguments = (Object[]) input.readObject();//获取方法的参数
									ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());//创建一个输出流
									try{
										Method method = service.getClass().getMethod(methodName, parameterTypes);
										Object result = method.invoke(service, arguments);//通过父类和参数类型得到子类
										output.writeObject(result);//将方法调用的结果输出到输出流中
									}catch(Throwable t){
										output.writeObject(t);
									}finally{
										output.close();
									}
								}finally{
									input.close();
								}
							}finally{
								socket.close();
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}).start();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/* 客户端调用
	 * 引用服务
	 * @param <T>接口泛型
	 * @param interfaceClass接口类型
	 * @param host 服务器主机名
	 * @param port 服务器端口
	 * @return 远程服务
	 */
	@SuppressWarnings("unchecked")
	public static <T> T refer(final Class<T> interfaceClass, final String host, final int port){
		if(interfaceClass == null){
			throw new IllegalArgumentException("Interface class == null");
		}
		if(! interfaceClass.isInterface()){
			throw new IllegalArgumentException("The " + interfaceClass.getName() + 
					" must be interface class!");
		}
		if(host == null || host.length() == 0){
			throw new IllegalArgumentException("Host == null!");
		}
		if(port <= 0 || port > 65535){
			throw new IllegalArgumentException("Invalid port " + port);
		}
		System.out.println("Get remote service " + interfaceClass.getName() + " form server " +
					host + ":" + port);
		//动态代理
		return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), 
					new Class<?>[]{interfaceClass}, 
					new InvocationHandler(){
				@Override
				public Object invoke(Object proxy, Method method,
						Object[] arguments) throws Throwable {
					Socket socket = new Socket(host, port);//连接服务端
					try{
						ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());//创建输出流
						try{
							output.writeUTF(method.getName());//将方法的名字写到服务端
							output.writeObject(method.getParameterTypes());//将方法的参数类型写到服务端
							output.writeObject(arguments);//将方法的参数写到服务端
							ObjectInputStream input = new ObjectInputStream(socket.getInputStream());//创建输入流
							try{
								Object result = input.readObject();//读取输入的对象
								if(result instanceof Throwable){
									throw (Throwable)result;
								}
								return result;//将对象返回
							}finally{
								input.close();
							}
						}finally{
							output.close();
						}
					}finally{
						socket.close();
					}
				}
			});
	}
}
