
package com.rpc.sample.server;


import com.rpc.client.HelloService;
import com.rpc.client.Person;
import com.rpc.server.RpcService;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/4 11:11
 * @Description:
 */

@RpcService("helloservice")
public class HelloServiceImpl implements HelloService{

    public String hello(String name){
        System.out.println("已经调用服务端接口实现，业务处理结果为：");
        System.out.println("hello!" + name);
        return "Hello ! "  + name;
    }

    public String hello(Person person){
        System.out.println("已经调用服务端接口实现： 业务处理为： ");
        System.out.println("Hello! " + person.getFirstName() + " " + person.getLastName());
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}

