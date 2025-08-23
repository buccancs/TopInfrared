package com.infisense.usbir.view;

public interface ITsTempListener {

    default float tempCorrectByTs(Float temp){
        return temp;
    }

}
