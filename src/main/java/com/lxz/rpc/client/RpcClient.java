package com.lxz.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lxz.rpc.commen.RpcDecoder;
import com.lxz.rpc.commen.RpcEncoder;
import com.lxz.rpc.commen.RpcRequest;
import com.lxz.rpc.commen.RpcResponse;

/*
 * Rpc 客户端（用于发送RPC请求）
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	private String host;
	private int port;
	private RpcResponse response;
	private final Object obj = new Object();

	public RpcClient(String host, int port){
		this.host = host;
		this.port = port;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response)
			throws Exception {
		this.response = response;
		
		synchronized(obj){
			obj.notifyAll();
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
		LOGGER.error("client caught exception", cause);
		ctx.close();
	}
	
	public RpcResponse send(RpcRequest request) throws InterruptedException{
		EventLoopGroup group = new NioEventLoopGroup();
		try{
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>(){
						@Override
						protected void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline()
								.addLast(new RpcEncoder(RpcRequest.class))
								.addLast(new RpcDecoder(RpcResponse.class))
								.addLast(RpcClient.this);
						}
					})
					.option(ChannelOption.SO_KEEPALIVE, true);
			
			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().writeAndFlush(request).sync();
			
			synchronized(obj){
				obj.wait();
			}
			
			if(response != null){
				future.channel().closeFuture().sync();
			}
			return response;
		}finally{
			group.shutdownGracefully();
		}
	}
}
