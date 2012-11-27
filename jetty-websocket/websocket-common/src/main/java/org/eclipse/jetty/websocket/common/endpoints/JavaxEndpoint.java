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
import javax.websocket.EndpointConfiguration;
import javax.websocket.Session;

import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.message.MessageHandlerCollection;

/**
 * Endpoint for objects that extend from {@link Endpoint javax.websocket.Endpoint}
 */
public class JavaxEndpoint extends AbstractEndpoint
{
    private final Endpoint websocket;

    public JavaxEndpoint(Endpoint endpoint)
    {
        super(endpoint);
        this.websocket = endpoint;
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration()
    {
        return websocket.getEndpointConfiguration();
    }

    @Override
    public void incomingError(WebSocketException e)
    {
        websocket.onError(e);
    }

    @Override
    public void incomingFrame(Frame frame)
    {
        try
        {
            switch (frame.getType())
            {
                case CLOSE:
                    CloseInfo close = new CloseInfo(frame);
                    websocket.onClose(close.asCloseReason());
                    break;
                default:
                    session.getMessageHandlerCollection().process(frame);
                    break;
            }
        }
        catch (Throwable t)
        {
            websocket.onError(t);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onOpen(Session session)
    {
        super.session.setMessageHandlerCollection(new MessageHandlerCollection());
        websocket.onOpen(session);
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]",this.getClass().getSimpleName(),websocket);
    }
}
