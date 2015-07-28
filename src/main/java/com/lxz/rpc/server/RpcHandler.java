package com.lxz.rpc.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lxz.rpc.commen.RpcRequest;
import com.lxz.rpc.commen.RpcResponse;

/*
 * RPC 处理器（用于处理RPC 请求）
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RpcHandler.class);
	private final Map<String, Object> handlerMap;
	
	public RpcHandler(Map<String, Object> handlerMap){
		this.handlerMap = handlerMap;
	}
	
	//当有数据读入的时候，自动调用
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request)
			throws Exception {
		RpcResponse response = new RpcResponse();//初始化响应对象
		response.setRequestId(request.getRequestId());//将请求对象的Id赋值给响应对象
		try{
			Object result = handle(request);//处理部分
			response.setResult(result);
		}catch(Throwable t){
			response.setError(t);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);//将结果写出去
	}
	
	//核心的处理部分
	private Object handle(RpcRequest request) throws InvocationTargetException{
		String className = request.getClassName();//获取请求的类的名称
		Object serviceBean = handlerMap.get(className);
		
		Class<?> serviceClass = serviceBean.getClass();//保存类名
		String methodName = request.getMethodName();//保存方法名
		Class<?>[] parameterTypes = request.getParameterTypes();//保存参数类型
		Object[] parameters = request.getParameters();//保存参数
		
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		return serviceFastMethod.invoke(serviceBean, parameters);//调用xxxImpl实现类
	}
	
	//当发生异常的时候netty会自动调用这个方法
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
		LOGGER.error("server caught exception", cause);
		ctx.close();
	}
}
