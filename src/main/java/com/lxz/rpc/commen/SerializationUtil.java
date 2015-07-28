package com.lxz.rpc.commen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.lxz.rpc.sample.client.Person;


/*
 * 序列化工具类（基于Protostuff实现）
 */
public class SerializationUtil {
	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
	private static Objenesis objenesis = new ObjenesisStd(true);
	
	private SerializationUtil(){
	}
	
	private static<T> Schema<T> getSchema(Class<T> cls){
		@SuppressWarnings("unchecked")
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if(schema == null){
			schema = RuntimeSchema.createFrom(cls);
			if(schema != null){
				cachedSchema.put(cls, schema);
			}
		}
		return schema;
	}
	
	/*
	 * 序列化（对象->字节数组）
	 * @param 传入需要序列化的对象
	 * @return 返回给序列化后的字节数组
	 */
	@SuppressWarnings("unchecked")
	public static<T> byte[] serialize(T obj){
		Class<T> cls = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try{
			Schema<T> schema = getSchema(cls);
			return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
		}catch(Exception e){
			throw new IllegalStateException(e.getMessage(), e);
		}finally{
			buffer.clear();
		}
	}
	
	/*
	 * 反序列化（字节数组->对象）
	 * @param data 传入序列化后的数组
	 * @param cls 传入需要反序列化的类
	 * @return 返回序列化后的对象
	 */
	public static <T> T deserialize(byte[] data, Class<T> cls){
		try{
			T message = (T) objenesis.newInstance(cls);
			Schema<T> schema = getSchema(cls);
			ProtostuffIOUtil.mergeFrom(data, message, schema);
			return message;
		}catch(Exception e){
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public static void main(String[] args){
		Person beforeSerPerson = new Person("Alexa", "Cloudera");//创建对象
		byte[] tmp = SerializationUtil.serialize(beforeSerPerson);//对其进行序列化
		Person afterSerPerson = (Person)SerializationUtil.deserialize(tmp, Person.class);//对其反序列化
		System.out.println(afterSerPerson.getFirstName() + " " + afterSerPerson.getLastName());
	}
}
