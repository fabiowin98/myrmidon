/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgl.myrmidon;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class MyrmidonServer extends Thread {

    private ServerSocket serverSocket;
    private Object targetObject;
    private int serverSocketPort;

    public MyrmidonServer(Object targetObject, int port) {
        this.targetObject = targetObject;
        this.serverSocketPort = port;
    }

    @Override
    public void run() {
        Socket socket;
        try {
            serverSocket = new ServerSocket(serverSocketPort);
            while (true) {
                socket = serverSocket.accept();
                new Thread(new MyrmidonServerHandler(targetObject,socket)).start();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void interrupt() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            //ignored
        } finally {
            super.interrupt(); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
