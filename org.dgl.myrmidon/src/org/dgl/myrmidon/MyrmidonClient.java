/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dgl.myrmidon;

import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class MyrmidonClient {
    
    private String ipAddress;
    private int port;
    
    public MyrmidonClient(String ipAddress, int port){
        this.ipAddress=ipAddress;
        this.port=port;
    }
    
    public Object invoke(String methodName, Object... args) throws Exception{
        Socket socket;
        ObjectOutputStream output;
        ObjectInputStream input;
        Object toRet=null;
        int outputCount;
        socket=new Socket(ipAddress, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        output.writeUTF(methodName);
        output.writeInt(args.length);
        for(Object arg : args){
            output.writeObject(arg);
        }
        outputCount = input.readInt();
        if(outputCount!=0){
            toRet = input.readObject();
        }
        output.flush();
        output.close();
        input.close();
        socket.close();
        return toRet;
    }
}
