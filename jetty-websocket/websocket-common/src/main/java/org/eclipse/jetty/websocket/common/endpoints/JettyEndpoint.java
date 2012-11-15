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

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.DefaultClientConfiguration;
import javax.websocket.EndpointConfiguration;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.WebSocketFrame;
import org.eclipse.jetty.websocket.common.message.MessageAppender;
import org.eclipse.jetty.websocket.common.message.SimpleBinaryMessage;
import org.eclipse.jetty.websocket.common.message.SimpleTextMessage;

/**
 * Endpoints that extends {@link WebSocketListener}
 */
public class JettyEndpoint extends AbstractJettyEndpoint
{
    private class BinaryMessageHandler implements MessageHandler.Basic<byte[]>
    {
        @Override
        public void onMessage(byte[] message)
        {
            websocket.onWebSocketBinary(message, 0, message.length);
        }
    }

    private class TextMessageHandler implements MessageHandler.Basic<String>
    {
        @Override
        public void onMessage(String message)
        {
            websocket.onWebSocketText(message);
        }
    }

    private final WebSocketListener websocket;
    private TextMessageHandler textMessageHandler;
    private BinaryMessageHandler binaryMessageHandler;
    private EndpointConfiguration configuration;
    private MessageAppender activeMessage;

    public JettyEndpoint(WebSocketListener listener)
    {
        super(listener);
        this.websocket = listener;
        // TODO: solve missing path problem
        this.configuration = new DefaultClientConfiguration(null);
    }

    private BinaryMessageHandler getBinaryMessageHandler()
    {
        if (binaryMessageHandler == null)
        {
            binaryMessageHandler = new BinaryMessageHandler();
        }

        return binaryMessageHandler;
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration()
    {
        return configuration;
    }

    private TextMessageHandler getTextMessageHandler()
    {
        if (textMessageHandler == null)
        {
            textMessageHandler = new TextMessageHandler();
        }

        return textMessageHandler;
    }


    public WebSocketListener getWebsocket()
    {
        return this.websocket;
    }

    @Override
    public void onBinaryFragment(ByteBuffer payload, boolean fin) throws IOException
    {
        if (activeMessage == null)
        {
            activeMessage = new SimpleBinaryMessage(getSession(),getBinaryMessageHandler());
        }

        activeMessage.appendMessage(payload);

        if (fin)
        {
            activeMessage.messageComplete();
            activeMessage = null;
        }
    }

    @Override
    public void onClose(CloseInfo close)
    {
        int statusCode = close.getStatusCode();
        String reason = close.getReason();
        this.websocket.onWebSocketClose(statusCode,reason);
    }

    @Override
    public void onException(WebSocketException e)
    {
        this.websocket.onWebSocketException(e);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onOpen(Session session)
    {
        this.websocket.onWebSocketConnect(getSession());
    }

    @Override
    public void onTextFragment(ByteBuffer payload, boolean fin) throws IOException
    {
        if (activeMessage == null)
        {
            activeMessage = new SimpleTextMessage(getSession(),getTextMessageHandler());
        }

        activeMessage.appendMessage(payload);

        if (fin)
        {
            activeMessage.messageComplete();
            activeMessage = null;
        }
    }

    public void setEndpointConfiguration(EndpointConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    protected void terminateConnection(int statusCode, String rawreason)
    {
        String reason = rawreason;
        reason = StringUtil.truncate(reason,(WebSocketFrame.MAX_CONTROL_PAYLOAD - 2));
        LOG.debug("terminateConnection({},{})",statusCode,rawreason);
        session.close(statusCode,reason);
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]", this.getClass().getSimpleName(), websocket);
    }
}
