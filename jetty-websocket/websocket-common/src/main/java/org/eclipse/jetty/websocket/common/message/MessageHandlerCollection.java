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

package org.eclipse.jetty.websocket.common.message;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.websocket.MessageHandler;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.api.extensions.Frame.Type;
import org.eclipse.jetty.websocket.common.message.platform.BinaryStreamMessageHandler;
import org.eclipse.jetty.websocket.common.message.platform.ByteArrayMessageHandler;
import org.eclipse.jetty.websocket.common.message.platform.ByteBufferMessageHandler;
import org.eclipse.jetty.websocket.common.message.platform.TextMessageHandler;
import org.eclipse.jetty.websocket.common.message.platform.TextStreamMessageHandler;

/**
 * Collection of {@link MessageHandler} implementations.
 */
public class MessageHandlerCollection
{
    private static final Logger LOG = Log.getLogger(MessageHandlerCollection.class);

    private LinkedList<MessageHandler> handlers = new LinkedList<>();
    private LinkedList<MessageHandler> platformHandlers = new LinkedList<>();
    private Frame.Type lastType = null;

    public MessageHandlerCollection()
    {
        // Setup platform handlers
        platformHandlers.add(new TextMessageHandler());
        platformHandlers.add(new ByteBufferMessageHandler());
        platformHandlers.add(new ByteArrayMessageHandler());
        platformHandlers.add(new TextStreamMessageHandler());
        platformHandlers.add(new BinaryStreamMessageHandler());
    }

    public void add(MessageHandler handler)
    {
        if (handlers.contains(handler))
        {
            throw new IllegalStateException("Handler already added: " + handler);
        }
        handlers.add(handler);
    }

    public LinkedList<MessageHandler> getHandlers()
    {
        return handlers;
    }

    public Set<MessageHandler> getHandlerSet()
    {
        Set<MessageHandler> ret = new HashSet<>();
        ret.addAll(handlers);
        return ret;
    }

    public void process(Frame frame)
    {
        switch (frame.getType())
        {
            case BINARY:
            {
                if (frame.isLast())
                {
                    // single binary message
                    processBinaryMessage(frame);
                }
                else
                {
                    // binary fragment
                    processBinaryFragment(frame);
                    lastType = Type.BINARY;
                }
                break;
            }
            case TEXT:
            {
                if (frame.isLast())
                {
                    // single text message
                    processTextMessage(frame);
                }
                else
                {
                    // text fragment
                    processTextFragment(frame);
                    lastType = Type.TEXT;
                }
                break;
            }
            case CONTINUATION:
            {
                if (lastType == Type.BINARY)
                {
                    // binary fragment
                    processBinaryFragment(frame);
                    if (frame.isLast())
                    {
                        lastType = null;
                    }
                }
                else if (lastType == Type.TEXT)
                {
                    // text fragment
                    processTextFragment(frame);
                    if (frame.isLast())
                    {
                        lastType = null;
                    }
                }
                else
                {
                    // invalid type
                    throw new WebSocketException("Invalid Continuation Frame Type (no last frame?)");
                }
                break;
            }
            default:
                throw new WebSocketException("Unhandled Frame Type: " + frame.getType());
        }

        // Toss Error
    }

    private void processBinaryFragment(Frame frame)
    {
        LOG.debug("processBinaryFragment({})",frame);
        // TODO Auto-generated method stub
    }

    private void processBinaryMessage(Frame frame)
    {
        LOG.debug("processBinaryMessage({})",frame);
        // TODO Auto-generated method stub
    }

    private void processTextFragment(Frame frame)
    {
        LOG.debug("processTextFragment({})",frame);
        // TODO Auto-generated method stub

        for (MessageHandler handler : handlers)
        {

        }
    }

    private void processTextMessage(Frame frame)
    {
        LOG.debug("processTextMessage({})",frame);
        // TODO Auto-generated method stub
    }

    public void remove(MessageHandler listener)
    {
        handlers.remove(listener);
    }
}
