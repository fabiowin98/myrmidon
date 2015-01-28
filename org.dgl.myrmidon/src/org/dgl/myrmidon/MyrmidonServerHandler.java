/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgl.myrmidon;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
class MyrmidonServerHandler implements Runnable {

    private Socket socket;
    private Object targetObject;

    public MyrmidonServerHandler(Object targetObject, Socket socket) {
        this.socket = socket;
        this.targetObject = targetObject;
    }

    @Override
    public void run() {
        ObjectInputStream input;
        ObjectOutputStream output;
        String methodName;
        ArrayList<Object> methodArgs;
        Method[] methods;
        int methodCount;
        Object ret;
        methodArgs = new ArrayList<>();
        try {
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            methodName = input.readUTF();
            methodCount = input.readInt();
            for (int i = methodCount; i > 0; i--) {
                methodArgs.add(input.readObject());
            }
            methods = targetObject.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    m.setAccessible(true);
                    synchronized (targetObject) {
                        ret = m.invoke(targetObject, methodArgs.toArray());
                    }
                    if (ret != null) {
                        output.writeInt(1);
                        output.writeObject(ret);
                    } else {
                        output.writeInt(0);
                    }
                }
            }
            output.flush();
            output.close();
            input.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
