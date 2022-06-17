package com.lmax.nanofix.incoming;


interface MessageParserCallbackTestFactory
{
    void onMessage(byte[] buffer, int offset, int length);

}
