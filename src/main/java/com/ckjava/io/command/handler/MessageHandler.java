package com.ckjava.io.command.handler;

import com.ckjava.io.command.Connection;

public interface MessageHandler {
    public void onReceive(Connection connection, String message);
}