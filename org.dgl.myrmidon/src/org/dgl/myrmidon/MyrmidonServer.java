/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgl.myrmidon;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 *
 * @author Administrator
 */
public class MyrmidonServer extends Thread {

    private ServerSocket serverSocket;
    private Object targetObject;
    private int serverSocketPort;
    private ArrayList<MyrmidonServerExceptionListener> exceptionListeners;

    public MyrmidonServer(Object targetObject, int port) {
        this.targetObject = targetObject;
        this.serverSocketPort = port;
        exceptionListeners = new ArrayList<>();
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(serverSocketPort);
            while (true) {
                socket = serverSocket.accept();
                new Thread(new MyrmidonServerHandler(targetObject, socket)).start();
            }
        } catch (Exception e) {
            for( MyrmidonServerExceptionListener exceptionListener : exceptionListeners) exceptionListener.CatchMyrmidonServerException(socket, e);
        }
    }

    @Override
    public void interrupt() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            for( MyrmidonServerExceptionListener exceptionListener : exceptionListeners) exceptionListener.CatchMyrmidonServerException(null, e);
        } finally {
            super.interrupt(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public void addMyrmidonServerExceptionListener(MyrmidonServerExceptionListener exceptionListener) {
        exceptionListeners.add(exceptionListener);
    }
}
