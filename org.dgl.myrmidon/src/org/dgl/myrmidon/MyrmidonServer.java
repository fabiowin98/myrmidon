/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgl.myrmidon;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Administrator
 */
public class MyrmidonServer extends Thread implements MyrmidonServerExceptionListener {

    private ServerSocket serverSocket;
    private Object targetObject;
    private int serverSocketPort;
    private ArrayList<MyrmidonServerExceptionListener> exceptionListeners;
    private HashMap<String, Integer> blackList;
    private int blackListLimit;
    private boolean blackListEnabled;
    public static final int DEFAULT_BLACKLISTLIMIT = 10;
    public static final boolean DEFAULT_ISBLACKLISTENABLED = false;

    public MyrmidonServer(Object targetObject, int port) {
        this.targetObject = targetObject;
        this.serverSocketPort = port;
        exceptionListeners = new ArrayList<>();
        blackList = new HashMap<>();
        blackListLimit = DEFAULT_BLACKLISTLIMIT;
        blackListEnabled = DEFAULT_ISBLACKLISTENABLED;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(serverSocketPort);
            while (true) {
                socket = serverSocket.accept();
                if (isNotBlacklisted(socket)) {
                    new Thread(new MyrmidonServerHandler(targetObject, socket, this)).start();
                } else {
                    socket.close();
                }
            }
        } catch (Exception e) {
            updateBlacklist(socket);
            for (MyrmidonServerExceptionListener exceptionListener : exceptionListeners) {
                exceptionListener.CatchMyrmidonServerException(socket, e);
            }
        }
    }

    @Override
    public void interrupt() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            for (MyrmidonServerExceptionListener exceptionListener : exceptionListeners) {
                exceptionListener.CatchMyrmidonServerException(null, e);
            }
        } finally {
            super.interrupt(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public void addMyrmidonServerExceptionListener(MyrmidonServerExceptionListener exceptionListener) {
        exceptionListeners.add(exceptionListener);
    }

    private void updateBlacklist(Socket socket) {
        String address;
        Integer value;
        if ((socket == null) || (!isBlackListEnabled())) {
            return;
        }
        address = socket.getInetAddress().getHostAddress();
        synchronized (blackList) {
            if (blackList.containsKey(address)) {
                value = blackList.remove(address);
                blackList.put(address, new Integer(value.intValue() + 1));
            } else {
                blackList.put(address, 1);
            }
        }
    }

    private boolean isNotBlacklisted(Socket socket) {
        String address;
        Integer value;
        if (!isBlackListEnabled()) {
            return true;
        }
        if (socket == null) {
            return false;
        }
        address = socket.getInetAddress().getHostAddress();
        synchronized (blackList) {
            if (blackList.containsKey(address)) {
                value = blackList.get(address);
                return value < getBlackListLimit();
            }
        }
        return true;
    }

    public int getBlackListLimit() {
        return blackListLimit;
    }

    public void setBlackListLimit(int blackListLimit) {
        this.blackListLimit = blackListLimit;
    }

    public HashMap<String, Integer> getBlackList() {
        return blackList;
    }

    public boolean isBlackListEnabled() {
        return blackListEnabled;
    }

    public void setBlackListEnabled(boolean blackListEnabled) {
        this.blackListEnabled = blackListEnabled;
    }

    @Override
    public void CatchMyrmidonServerException(Socket source, Exception ex) {
        updateBlacklist(source);
        for (MyrmidonServerExceptionListener exceptionListener : exceptionListeners) {
            exceptionListener.CatchMyrmidonServerException(source, ex);
        }
    }
}
