package com.rpc.app;

import com.rpc.client.HelloService;
import com.rpc.client.Person;
import com.rpc.client.RpcProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/5 14:44
 * @Description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1(){
        //调用代理的create方法，代理HelloService接口
       HelloService helloService = rpcProxy.create(HelloService.class);

       //调用代理的方法，执行invoke
       String result = helloService.hello("World");
        System.out.println("服务端返回结果");
        System.out.println(result);
    }

    @Test
    public void helloTest2(){
        //调用代理的create方法，代理HelloService接口
       HelloService helloService = rpcProxy.create(HelloService.class);

       //调用代理的方法，执行invoke
       String result = helloService.hello(new Person("Yangyang", "Pang"));
        System.out.println("服务端返回结果");
        System.out.println(result);
    }
}
