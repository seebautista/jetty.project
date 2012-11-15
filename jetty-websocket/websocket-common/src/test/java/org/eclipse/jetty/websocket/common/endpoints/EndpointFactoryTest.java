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

import static org.hamcrest.Matchers.*;

import javax.websocket.Endpoint;
import javax.websocket.WebSocketEndpoint;

import org.eclipse.jetty.websocket.api.InvalidWebSocketException;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.common.annotations.NotASocket;
import org.junit.Assert;
import org.junit.Test;

import examples.AdapterConnectCloseSocket;
import examples.ListenerBasicSocket;

public class EndpointFactoryTest
{
    private void assertInvalidWebSocketMessage(InvalidWebSocketException e)
    {
        // Validate that we have clear error message to the developer.
        // Make sure that all forms of valid websocket are mentioned.
        Assert.assertThat(e.getMessage(),
                allOf(
                        containsString(WebSocketListener.class.getSimpleName()),
                        containsString(WebSocket.class.getSimpleName()),
                        containsString(WebSocketEndpoint.class.getSimpleName()),
                        containsString(Endpoint.class.getSimpleName())));
    }

    /**
     * Test Case for bad declaration (duplicate OnWebSocketBinary declarations)
     */
    @Test
    public void testBadNotASocket()
    {
        try
        {
            NotASocket bad = new NotASocket();
            // Should toss exception
            EndpointFactory.create(bad);
        }
        catch (InvalidWebSocketException e)
        {
            assertInvalidWebSocketMessage(e);
        }
    }

    /**
     * Jetty extends WebSocketAdapter, as JettyEndpoint
     */
    @Test
    public void testExtendsJettyWebSocketAdapter()
    {
        AdapterConnectCloseSocket websocket = new AdapterConnectCloseSocket();
        AbstractEndpoint endpoint = EndpointFactory.create(websocket);

        String classId = AdapterConnectCloseSocket.class.getSimpleName();
        Assert.assertThat("Endpoint for " + classId,endpoint,instanceOf(JettyEndpoint.class));
    }

    /**
     * Test Case for no exceptions and 5 methods (implement WebSocketListener)
     */
    @Test
    public void testImplementsJettyWebsocketListener()
    {
        ListenerBasicSocket websocket = new ListenerBasicSocket();
        AbstractEndpoint endpoint = EndpointFactory.create(websocket);

        String classId = ListenerBasicSocket.class.getSimpleName();
        Assert.assertThat("Endpoint for " + classId,endpoint,instanceOf(JettyEndpoint.class));
    }
}
