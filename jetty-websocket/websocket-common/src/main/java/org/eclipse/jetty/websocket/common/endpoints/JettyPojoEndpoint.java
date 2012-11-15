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
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

import javax.websocket.DefaultClientConfiguration;
import javax.websocket.EndpointConfiguration;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.message.MessageAppender;
import org.eclipse.jetty.websocket.common.message.MessageInputStream;
import org.eclipse.jetty.websocket.common.message.MessageReader;
import org.eclipse.jetty.websocket.common.message.SimpleBinaryMessage;
import org.eclipse.jetty.websocket.common.message.SimpleTextMessage;

/**
 * Endpoint for POJOs annotated using Jetty's {@link WebSocket &#064;WebSocket}
 */
public class JettyPojoEndpoint extends AbstractJettyEndpoint
{
    private class BinaryMessageHandler implements MessageHandler.Basic<byte[]>
    {
        @Override
        public void onMessage(byte[] data)
        {
            if (metadata.onBinary != null)
            {
                metadata.onBinary.call(websocket,session,data,0,data.length);
            }
        }
    }

    private class InputStreamMessageHandler implements MessageHandler.Basic<InputStream>
    {
        @Override
        public void onMessage(InputStream stream)
        {
            if (metadata.onBinary != null)
            {
                metadata.onBinary.call(websocket,session,stream);
            }
        }
    }

    private class ReaderMessageHandler implements MessageHandler.Basic<Reader>
    {
        @Override
        public void onMessage(Reader reader)
        {
            if (metadata.onText != null)
            {
                metadata.onText.call(websocket,session,reader);
            }
        }
    }

    private class TextMessageHandler implements MessageHandler.Basic<String>
    {
        @Override
        public void onMessage(String message)
        {
            if (metadata.onText != null)
            {
                metadata.onText.call(websocket,session,message);
            }
        }
    }

    private final Object websocket;
    private final JettyPojoMetadata metadata;
    private EndpointConfiguration configuration;
    private TextMessageHandler textMessageHandler;
    private ReaderMessageHandler readerMessageHandler;
    private BinaryMessageHandler binaryMessageHandler;
    private InputStreamMessageHandler inputStreamMessageHandler;
    private MessageAppender activeMessage;

    public JettyPojoEndpoint(Object websocket)
    {
        super(websocket);
        this.websocket = websocket;
        this.metadata = JettyPojoAnnotationCache.discover(websocket);

        // TODO: solve missing path problem
        this.configuration = new DefaultClientConfiguration(null);

        WebSocket anno = websocket.getClass().getAnnotation(WebSocket.class);
        // Setup the policy
        if (anno.maxBufferSize() > 0)
        {
            this.policy.setBufferSize(anno.maxBufferSize());
        }
        if (anno.maxMessageSize() > 0)
        {
            this.policy.setMaxMessageSize(anno.maxMessageSize());
        }
        if (anno.idleTimeout() > 0)
        {
            this.policy.setIdleTimeout(anno.idleTimeout());
        }
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
        return this.configuration;
    }

    private InputStreamMessageHandler getInputStreamMessageHandler()
    {
        if(inputStreamMessageHandler == null) {
            inputStreamMessageHandler = new InputStreamMessageHandler();
        }
        return inputStreamMessageHandler;
    }

    public JettyPojoMetadata getMetadata()
    {
        return metadata;
    }

    private ReaderMessageHandler getReaderMessageHandler()
    {
        if(readerMessageHandler == null) {
            readerMessageHandler = new ReaderMessageHandler();
        }
        return readerMessageHandler;
    }

    private TextMessageHandler getTextMessageHandler()
    {
        if (textMessageHandler == null)
        {
            textMessageHandler = new TextMessageHandler();
        }

        return textMessageHandler;
    }

    @Override
    public void onBinaryFragment(ByteBuffer payload, boolean fin) throws IOException
    {
        if (metadata.onBinary == null)
        {
            // not interested in binary events
            return;
        }

        if (activeMessage == null)
        {
            if (metadata.onBinary.isStreaming())
            {
                activeMessage = new MessageInputStream(getSession(),getInputStreamMessageHandler());
            }
            else
            {
                activeMessage = new SimpleBinaryMessage(getSession(),getBinaryMessageHandler());
            }
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
        if (metadata.onClose != null)
        {
            metadata.onClose.call(websocket,session,close.getStatusCode(),close.getReason());
        }
    }

    @Override
    public void onException(WebSocketException e)
    {
        if (metadata.onException != null)
        {
            metadata.onException.call(websocket,session,e);
        }
    }

    @Override
    public void onFrame(Frame frame)
    {
        if (metadata.onFrame != null)
        {
            metadata.onFrame.call(websocket,session,frame);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onOpen(Session session)
    {
        if (metadata.onConnect != null)
        {
            metadata.onConnect.call(websocket,session);
        }
    }

    @Override
    public void onTextFragment(ByteBuffer payload, boolean fin) throws IOException
    {
        if (metadata.onText == null)
        {
            // not interested in text events
            return;
        }

        if (activeMessage == null)
        {
            if (metadata.onText.isStreaming())
            {
                activeMessage = new MessageReader(getSession(),getReaderMessageHandler());
            }
            else
            {
                activeMessage = new SimpleTextMessage(getSession(),getTextMessageHandler());
            }
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
    public String toString()
    {
        return String.format("%s[%s]", this.getClass().getSimpleName(), websocket);
    }
}
