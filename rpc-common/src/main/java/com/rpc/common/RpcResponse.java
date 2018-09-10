package com.rpc.common;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/5 10:43
 * @Description: 封装相应的Object
 */
public class RpcResponse {

    private String requestId;
    private Throwable error;
    private Object result;

    public boolean isError(){
        return error != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
