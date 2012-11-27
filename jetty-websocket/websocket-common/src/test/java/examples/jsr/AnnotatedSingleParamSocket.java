//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package examples.jsr;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.WebSocketEndpoint;
import javax.websocket.WebSocketMessage;
import javax.websocket.WebSocketPathParam;

/**
 * Parameterized Socket showing @WebSocketMessage declarations
 */
@WebSocketEndpoint("/params/{a}")
public class AnnotatedSingleParamSocket
{
    private List<String> events = new ArrayList<>();

    private void addEvent(String format, Object... args)
    {
        events.add(String.format(format,args));
    }

    @WebSocketMessage
    public void onByteArray(@WebSocketPathParam("a") String a, byte buf[])
    {
        addEvent("onByteArray(String a, byte buf[])");
    }

    @WebSocketMessage
    public void onText(@WebSocketPathParam("a") String a, String message)
    {
        addEvent("onTextMessage(String a, String message)");
    }
}
