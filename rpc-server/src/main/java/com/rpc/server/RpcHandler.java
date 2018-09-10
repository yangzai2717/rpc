package com.rpc.server;

import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/5 09:26
 * @Description:
 * 处理具体的业务调用
 * 通过构造时传入的“业务接口及实现”handlerMap,来调用客户端所请求的业务方法
 * 并将业务方法返回值封装成response对象下入下一个handler（即编码handler--RpcEncoder）
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }


    /**
     * 接受消息，处理消息，返回结果
     * @param ctx
     * @param
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest) msg;
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            //根据request来处理具体的业务调用
            Object result = handle(rpcRequest);
            rpcResponse.setResult(result);
        } catch (Throwable t){
            rpcResponse.setError(t);
        }
        //写入outbundle(即RpcEncoder) 进行下一步处理（即编码）后发送到channel中给客户端
        ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 根据request 来处理具体的业务调用
     * 调用时通过反射的方式来完成
     *
     * @param request
     * @return
     */
    private Object handle(RpcRequest request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String className = request.getClassName();

        //拿到实体类对象
        Object serviceBean = handlerMap.get(className);

        //拿到要调用的方法名，参数类型，参数值
        String methodName = request.getMethodName();
        Class<?>[] parameterType = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //拿到接口类
        Class<?>  forName = Class.forName(className);

        //调用实现类对象的指定方法并返回结果
        Method method = forName.getMethod(methodName, parameterType);
        return method.invoke(serviceBean, parameters);
    }


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {

    }
}
