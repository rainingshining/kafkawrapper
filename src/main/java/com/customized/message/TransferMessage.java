package com.customized.message;

import com.customized.util.TransCodeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 主、副消息实体类
 *
 *  * @author liangpei
 *  * @desc
 */
public class TransferMessage {

    //流程编码
    private String flowNo;
    //业务流水号
    private String businessNo;
    //主决策服务进件原始报文，需在处理前设置
    private String requestObjStr;
    //外数返回的原始报文
    private Map<String, String> outData = new HashMap<>();

    public String getFlowNo() {
        return flowNo;
    }

    public void setFlowNo(String flowNo) {
        this.flowNo = flowNo;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public String getRequestObjStr() {
        return requestObjStr;
    }

    public void setRequestObjStr(String requestObjStr) {
        this.requestObjStr = requestObjStr;
    }

    public Map<String, String> getOutData() {
        return outData;
    }

    public void setOutData(Map<String, String> outData) {
        this.outData = outData;
    }

    //已做解密处理
    public Map<String, String> readableOutDataMap(){
        Map<String, String> result = new HashMap<>();
        this.outData.forEach((k, v) -> {
            result.put(k, TransCodeUtil.decryptWithAESCBCMode(v));
        });
        return result;
    }

    //已解密
    public String readableRequestObj(){
        return TransCodeUtil.decryptWithAESCBCMode(this.requestObjStr);
    }

    //已做解密处理
    public String readableOutDataElementByCode(String interfaceCode){
        return TransCodeUtil.decryptWithAESCBCMode(this.outData.get(interfaceCode));
    }

}
