/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dgl.myrmidon;

import java.net.Socket;

/**
 *
 * @author Administrator
 */
public interface MyrmidonServerExceptionListener {
    public void CatchMyrmidonServerException(Socket source, Exception ex);
}
