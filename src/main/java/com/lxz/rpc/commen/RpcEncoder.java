package com.lxz.rpc.commen;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/*
 * RPC编码器
 */
public class RpcEncoder extends MessageToByteEncoder{
	private Class<?> genericClass;
	
	public RpcEncoder(Class<?> genericClass){
		this.genericClass = genericClass;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out)
			throws Exception {
		if(genericClass.isInstance(in)){
			byte[] data = SerializationUtil.serialize(in);//对打算编码的数据先序列化
			out.writeInt(data.length);//保存数据的长度
			out.writeBytes(data);//将编码数据输出
		}
	}

}
