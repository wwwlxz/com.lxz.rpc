package com.lxz.rpc.commen;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/*
 * RPC解码器
 */
public class RpcDecoder extends ByteToMessageDecoder{
	private Class<?> genericClass;
	
	public RpcDecoder(Class<?> genericClass){
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		if(in.readableBytes() < 4){
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();//读取出之前写入的数据长度
		if(dataLength < 0){//如果没有读取到数据则关闭
			ctx.close();
		}
		if(in.readableBytes() < dataLength){
			in.resetReaderIndex();
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);
		
		Object obj = SerializationUtil.deserialize(data, genericClass);//反序列化
		out.add(obj);//将反序列化的对象输出
	}
}
