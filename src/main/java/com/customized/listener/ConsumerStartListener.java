package com.customized.listener;

/**
 * ConsumerStartListener
 *
 * @author liangpei
 * @desc
 */
public interface ConsumerStartListener {
    void onStartSuccess();

    void onStartFailed(Throwable e);
}
