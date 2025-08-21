package com.infisense.usbir.view;


/**
 * 单点矫正温度,目前TS001和M256产品需要用到
 * @author: CaiSongL
 * @date: 2023/11/3 14:03
 */
public interface ITsTempListener {

    default float tempCorrectByTs(Float temp){
        return temp;
    }

}
