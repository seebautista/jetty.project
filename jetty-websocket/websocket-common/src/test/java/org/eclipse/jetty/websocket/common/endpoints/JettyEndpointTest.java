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

package org.eclipse.jetty.websocket.common.endpoints;

import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.WebSocketFrame;
import org.eclipse.jetty.websocket.common.io.LocalWebSocketSession;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import examples.AdapterConnectCloseSocket;
import examples.ListenerBasicSocket;

public class JettyEndpointTest
{
    @Rule
    public TestName testname = new TestName();

    private AbstractEndpoint createEndpoint(WebSocketListener socket)
    {
        AbstractEndpoint endpoint = new JettyEndpoint(socket);
        endpoint.setPolicy(WebSocketPolicy.newClientPolicy());
        return endpoint;
    }

    @Test
    public void testEvents_AdapterConnectClose()
    {
        AdapterConnectCloseSocket socket = new AdapterConnectCloseSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(2);
        socket.capture.assertEventStartsWith(0,"onWebSocketConnect");
        socket.capture.assertEventStartsWith(1,"onWebSocketClose");
    }

    @Test
    public void testEvents_ListenerBasic()
    {
        ListenerBasicSocket socket = new ListenerBasicSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(WebSocketFrame.text("Hello World"));
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onWebSocketConnect");
        socket.capture.assertEventStartsWith(1,"onWebSocketText(\"Hello World\")");
        socket.capture.assertEventStartsWith(2,"onWebSocketClose(1000,");
    }
}
