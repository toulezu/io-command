package com.ckjava.io.command;

public interface MessageHandler {
    public void onReceive(Connection connection, String message);
}