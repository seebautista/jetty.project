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

import javax.websocket.Endpoint;

import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.WebSocketFrame;
import org.eclipse.jetty.websocket.common.io.LocalWebSocketSession;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import examples.jsr.EndpointConnectCloseSocket;
import examples.jsr.EndpointSimpleSocket;

public class JavaxEndpointTest
{
    @Rule
    public TestName testname = new TestName();

    private AbstractEndpoint createEndpoint(Endpoint socket)
    {
        AbstractEndpoint endpoint = new JavaxEndpoint(socket);
        endpoint.setPolicy(WebSocketPolicy.newClientPolicy());
        return endpoint;
    }

    @Test
    public void testEndpointBasicBinaryMessage()
    {
        EndpointSimpleSocket socket = new EndpointSimpleSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(WebSocketFrame.binary(new byte[]
                { 0x12, 0x34, 0x56, 0x78, (byte)0x90 }));
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onOpen");
        socket.capture.assertEventStartsWith(1,"onBinary(byte[]: 1234567890)");
        socket.capture.assertEventStartsWith(2,"onClose");
    }

    @Test
    public void testEndpointBasicTextMessage()
    {
        EndpointSimpleSocket socket = new EndpointSimpleSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(WebSocketFrame.text("Hello World"));
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onOpen");
        socket.capture.assertEventStartsWith(1,"onText(\"Hello World\")");
        socket.capture.assertEventStartsWith(2,"onClose");
    }

    @Test
    public void testEndpointConnectClose()
    {
        EndpointConnectCloseSocket socket = new EndpointConnectCloseSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(2);
        socket.capture.assertEventStartsWith(0,"onOpen");
        socket.capture.assertEventStartsWith(1,"onClose");
    }
}
