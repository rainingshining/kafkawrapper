package com.customized.callback;

/**
 * Default callback for producer
 *
 * @author liangpei
 * @desc 默认发送回调类，根据实际情况继承并重写父类onFailure方法，并设置message等内容，可做发送失败处理；或
 * 继承ProduceCallBack，并重写onCompletion与onFailure
 */
public class DefaultProduceCallback extends ProduceCallBack{
}
