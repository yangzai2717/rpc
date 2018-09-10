package com.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/4 17:24
 * @Description: RPC 解码器
 */
public class RpcDecoder extends ByteToMessageDecoder{

    private Class<?> genericClass;

    //构造函数传入想反序列化的class
    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4){   //netty里面小于4个字节 就是没有内容
            return;
        }
        in.markReaderIndex();   //从哪里读标记一下
        int dataLength = in.readInt();  //byteBuf 里面有多少数据
        if(dataLength < 0){
            ctx.close();
        }
        if(in.readableBytes() < dataLength){   //处理tcp包不全的情况
            in.resetReaderIndex();
        }
        //将ByteBuf转换为byte[]
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        //将data转换成object
        Object obj = SerializationUtil.deserialize(data);
        out.add(obj);
    }
}
