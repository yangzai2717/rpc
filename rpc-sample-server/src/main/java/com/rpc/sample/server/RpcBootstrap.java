package com.rpc.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/7 17:46
 * @Description:
 * 用户系统服务端的启动入口
 * 其意义是启动springcontext，从而构造框架中的RpcServer
 * 亦即：将用户系统中所有标注RpcService注解的业务发布到RpcServer中
 */
public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
