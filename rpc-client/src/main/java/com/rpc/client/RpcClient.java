package com.rpc.client;

import com.rpc.common.RpcDecoder;
import com.rpc.common.RpcEncoder;
import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/5 16:53
 * @Description:
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private String host;
    private int port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 链接服务器，发送消息
     * @return
     */
    public RpcResponse send(RpcRequest request) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //向pipeline中添加编码，解码，业务处理的handler
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))  //out -1
                                    .addLast(new RpcDecoder(RpcResponse.class))  //in -1
                                    .addLast(RpcClient.this);                    //in -2
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);
            //连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //将request对象写入outbundle处理后发出（即RpcEncoder编码器）
            future.channel().writeAndFlush(request).sync();

            //用线程等待的方式决定是否关闭连接
            //其意义是，先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (obj){
                obj.wait();
            }
            if(response != null){
                future.channel().closeFuture().sync();
            }
            return response;
        }  finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 读取服务端的返回结果
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse response = (RpcResponse) msg;
        this.response = response;

        synchronized (obj){
            obj.notifyAll();
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {

    }
}
