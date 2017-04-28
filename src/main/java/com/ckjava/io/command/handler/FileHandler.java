package com.ckjava.io.command.handler;

import com.ckjava.io.command.Connection;

public interface FileHandler {
    public void onReceive(Connection connection, String fileName, String message);
}