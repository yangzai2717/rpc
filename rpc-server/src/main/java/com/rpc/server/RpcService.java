package com.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/8/31 10:23
 * @Description: 自定义注解
 */

@Target( ElementType.TYPE) //注解用在接口上
@Retention(RetentionPolicy.RUNTIME) //vm将在运行期也保留注释，因此可以通过反射机制读取注解的信息
@Component
public @interface RpcService {

    String value();

}
