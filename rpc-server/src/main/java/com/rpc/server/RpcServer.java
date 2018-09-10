package com.rpc.server;

import com.rpc.common.RpcDecoder;
import com.rpc.common.RpcEncoder;
import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import com.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/4 14:39
 * @Description:
 * 框架的RPC 服务器 （用于将用户系统的业务类发布为RPC 服务）
 * 使用时可由用户通过spring-bean的方式注入到用户的业务系统中
 * 由于本类实现了applicationcontextAware 和 InitializingBean
 * spring构造本对象时会调用setApplicationContext方法，从而可以在方法中通过自定义注解获得用户的业务接口和实现类
 * 还会调用afterPropertiesSet方法，在方法中启动netty服务器
 */
public class RpcServer implements ApplicationContextAware, InitializingBean{

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    //用于存储业务接口和实现类的实例对象（由spring所构造）
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    //服务器绑定的地址和接口由spring在构造本类事从配置文件中传入
    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        //用于向zookeeper注册名称服务的工具类
        this.serviceRegistry = serviceRegistry;
    }


    /**
     * 通过注解 获取标注了rpc服务注解的业务类的-----接口以及impl对象，将它放到handlerMap中
     * @throws Exception
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if(!CollectionUtils.isEmpty(serviceBeanMap)){
            for (Object serviceBean : serviceBeanMap.values()) {
                //从业务实现类上的自定义注解中获取到value,从而获取到业务接口的全面
                String interfaceName = serviceBean.getClass()
                        .getAnnotation(RpcService.class).value();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    /**
     * 在此启动netty服务，绑定handle流水线
     * 1.接受请求数据进行反序列化得到request对象
     * 2·根据request中的参数，让RpcHandler从handlerMap中找到对应的业务impl，调用指定的方法
     * 3.将业务调用结果封装到response并序列化后发往客户端
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))  //注册解码 IN-1
                                    .addLast(new RpcEncoder(RpcResponse.class))  //注册编码 out
                                    .addLast(new RpcHandler(handlerMap)); //注册Rpchandler IN-2
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.debug("server started on port {}", port);

            if(serviceRegistry != null){
                serviceRegistry.register(serverAddress);  //向zookeeper 注册
            }

            future.channel().closeFuture().sync();
        }catch (Exception e){
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }



}
