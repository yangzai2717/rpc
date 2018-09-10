package com.rpc.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/5 09:18
 * @Description:
 */
//TODO
public class SerializationUtil {

    private static Object objenesis;

    /**
     * 序列化 （对象  ->  字节数组）
     * @param obj
     * @return
     */
    public static byte[] serialize(Object obj){
        byte[] bytes = null;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = null;
        try {
            oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                bo.close();
                oo.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return bytes;
    }


    /**
     * 反序列化 （字节数组  ->  对象）
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static Object  deserialize(byte[] bytes){
        Object obj = null;
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = null;
        try {
            oi = new ObjectInputStream(bi);
            obj = oi.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                bi.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                oi.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return obj;
    }
}
