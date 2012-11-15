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

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.MessageHandler;

import org.eclipse.jetty.util.Utf8StringBuilder;
import org.eclipse.jetty.websocket.common.WebSocketSession;

public class SimpleTextMessage implements MessageAppender
{
    private final WebSocketSession session;
    private final MessageHandler.Basic<String> onEvent;
    private final Utf8StringBuilder utf;
    private int size = 0;
    private boolean finished;

    public SimpleTextMessage(WebSocketSession session, MessageHandler.Basic<String> onEvent)
    {
        this.session = session;
        this.onEvent = onEvent;
        this.utf = new Utf8StringBuilder();
        size = 0;
        finished = false;
    }

    @Override
    public void appendMessage(ByteBuffer payload) throws IOException
    {
        if (finished)
        {
            throw new IOException("Cannot append to finished buffer");
        }

        if (payload == null)
        {
            // empty payload is valid
            return;
        }

        session.assertValidMessageSize(size + payload.remaining());
        size += payload.remaining();

        // allow for fast fail of BAD utf (incomplete utf will trigger on messageComplete)
        this.utf.append(payload);
    }

    @Override
    public void messageComplete()
    {
        finished = true;

        // notify event
        onEvent.onMessage(utf.toString());
    }
}
