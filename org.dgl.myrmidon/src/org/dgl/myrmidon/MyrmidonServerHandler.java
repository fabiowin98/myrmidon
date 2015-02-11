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
    private MyrmidonServerExceptionListener exceptionListener;

    public MyrmidonServerHandler(Object targetObject, Socket socket, MyrmidonServerExceptionListener exceptionListener) {
        this.socket = socket;
        this.targetObject = targetObject;
        this.exceptionListener = exceptionListener;
    }

    @Override
    public void run() {
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        String methodName;
        ArrayList<Object> methodArgs;
        Method[] methods;
        int methodCount;
        Object ret;
        boolean found = false;
        Exception exception = null;
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
                if ((m.getName().equals(methodName)) && (m.getParameterTypes().length == methodArgs.size())) {
                    found = true;
                    m.setAccessible(true);
                    ret = null;
                    try {
                        synchronized (targetObject) {
                            ret = m.invoke(targetObject, methodArgs.toArray());
                        }
                    } catch (Exception ex) {
                        exception = ex;
                        output.writeInt(-1);
                        output.writeObject(exception);
                        break;
                    }
                    if (ret != null) {
                        output.writeInt(1);
                        output.writeObject(ret);
                    } else {
                        output.writeInt(0);
                    }
                    break;
                }
            }
            if (!found) {
                exception = new Exception("method not found");
                output.writeInt(-1);
                output.writeObject(exception);
            }
            output.flush();
            output.close();
            input.close();
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        } finally {
            if ((exceptionListener != null) && (exception != null)) {
                exceptionListener.CatchMyrmidonServerException(socket, exception);
            }
        }
    }

}
