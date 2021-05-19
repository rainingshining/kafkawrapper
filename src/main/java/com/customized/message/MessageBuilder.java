package com.customized.message;

import com.alibaba.fastjson.JSON;
import com.customized.util.TransCodeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 基本建造者类，用于处理外数分次调用问题，减少决策服务代码开发
 * @author Liangpei
 * @desc
 */
public class MessageBuilder {

    private TransferMessage transferMessage;
    private Map<String, String> outData = new HashMap<>();

    public static MessageBuilder builder(){
        return new MessageBuilder();
    }

    private MessageBuilder() {
        this.transferMessage = new TransferMessage();
    }

    public MessageBuilder flowNumber(String number){
        this.transferMessage.setFlowNo(number);
        return this;
    }

    public MessageBuilder businessNumber(String number){
        this.transferMessage.setBusinessNo(number);
        return this;
    }

    public MessageBuilder requestObj(String obj){
        this.transferMessage.setRequestObjStr(TransCodeUtil.encryptWithAESCBCMode(obj));
        return this;
    }

    public MessageBuilder outData(String key, String value){
        this.outData.put(key, TransCodeUtil.encryptWithAESCBCMode(value));
        return this;
    }

    //投递消息前调用，并将实体转换为json字符串
    public TransferMessage build(){
        this.transferMessage.setOutData(this.outData);
        return this.transferMessage;
    }

    //直接生成json字符串
    public String buildJsonString(){
        TransferMessage build = this.build();
        return JSON.toJSONString(build);
    }

}
