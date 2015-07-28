package com.lxz.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.lxz.rpc.commen.RpcDecoder;
import com.lxz.rpc.commen.RpcEncoder;
import com.lxz.rpc.commen.RpcRequest;
import com.lxz.rpc.commen.RpcResponse;

/*
 * RPC 服务器（用于发布RPC服务）
 */
public class RpcServer implements ApplicationContextAware, InitializingBean{
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
	private String serverAddress;
	private Map<String, Object> handlerMap = new HashMap<>();
	
	public RpcServer(String serverAddress){
		this.serverAddress = serverAddress;
	} 
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
		if(MapUtils.isNotEmpty(serviceBeanMap)){
			for(Object serviceBean : serviceBeanMap.values()){
				String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
				handlerMap.put(interfaceName, serviceBean);
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		//创建netty
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try{
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>(){
						@Override
						protected void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline()
								.addLast(new RpcDecoder(RpcRequest.class))//对发出的请求加密
								.addLast(new RpcEncoder(RpcResponse.class))//对返回的响应解密解密
								.addLast(new RpcHandler(handlerMap));//处理过程
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			
			ChannelFuture future = bootstrap.bind(host, port).sync();
			LOGGER.debug("server started on port {}", port);
			future.channel().closeFuture().sync();
		}finally{
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
