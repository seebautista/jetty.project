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

import javax.websocket.Session;
import javax.websocket.WebSocketEndpoint;
import javax.websocket.WebSocketMessage;

/**
 * Simple Socket showing @WebSocketMessage declarations, with optional Session as first param
 */
@SuppressWarnings("rawtypes")
@WebSocketEndpoint("/simple")
public class AnnotatedPreSessionSimpleSocket
{
    private List<String> events = new ArrayList<>();

    private void addEvent(String format, Object... args)
    {
        events.add(String.format(format,args));
    }

    @WebSocketMessage
    public void onByteArray(Session session, byte buf[])
    {
        addEvent("onByteArray(Session session, byte buf[])");
    }

    @WebSocketMessage
    public void onText(Session session, String message)
    {
        addEvent("onTextMessage(Session session, String message)");
    }
}
