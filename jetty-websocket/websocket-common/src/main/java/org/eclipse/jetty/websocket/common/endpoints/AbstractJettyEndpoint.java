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

import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.Utf8Appendable.NotUtf8Exception;
import org.eclipse.jetty.websocket.api.BadPayloadException;
import org.eclipse.jetty.websocket.api.CloseException;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.OpCode;
import org.eclipse.jetty.websocket.common.WebSocketFrame;

/**
 * Abstract Endpoint handler for Jetty websockets
 * 
 * @see JettyEndpoint
 * @see JettyPojoEndpoint
 */
public abstract class AbstractJettyEndpoint extends AbstractEndpoint
{
    public AbstractJettyEndpoint(Object websocket)
    {
        super(websocket);
    }

    @Override
    public final void incomingError(WebSocketException e)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("incoming(WebSocketException)",e);
        }

        if (e instanceof CloseException)
        {
            CloseException close = (CloseException)e;
            terminateConnection(close.getStatusCode(),close.getMessage());
        }

        onException(e);
    }

    @Override
    public void incomingFrame(Frame frame)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("incomingFrame({})",frame);
        }

        onFrame(frame);

        try
        {
            switch (frame.getType().getOpCode())
            {
                case OpCode.CLOSE:
                {
                    boolean validate = true;
                    CloseInfo close = new CloseInfo(frame,validate);

                    // notify user websocket pojo
                    onClose(close);

                    // respond
                    session.close(close.getStatusCode(),close.getReason());

                    // process handshake
                    session.getConnection().onCloseHandshake(true,close);

                    return;
                }
                case OpCode.PING:
                {
                    ByteBuffer pongBuf = ByteBuffer.allocate(frame.getPayloadLength());
                    if (frame.getPayloadLength() > 0)
                    {
                        // Copy payload
                        BufferUtil.clearToFill(pongBuf);
                        BufferUtil.put(frame.getPayload(),pongBuf);
                        BufferUtil.flipToFlush(pongBuf,0);
                    }
                    session.getRemote().sendPong(pongBuf);
                    break;
                }
                case OpCode.BINARY:
                {
                    onBinaryFragment(frame.getPayload(),frame.isFin());
                    return;
                }
                case OpCode.TEXT:
                {
                    onTextFragment(frame.getPayload(),frame.isFin());
                    return;
                }
            }
        }
        catch (NotUtf8Exception e)
        {
            LOG.debug(e);
            onException(new BadPayloadException(e));
            terminateConnection(StatusCode.BAD_PAYLOAD,e.getMessage());
        }
        catch (CloseException e)
        {
            LOG.debug(e);
            onException(e);
            terminateConnection(e.getStatusCode(),e.getMessage());
        }
        catch (WebSocketException e)
        {
            LOG.debug(e);
            onException(e);
            unhandled(e);
        }
        catch (Throwable t)
        {
            LOG.debug(t);
            onException(new WebSocketException(t));
            unhandled(t);
        }
    }

    public abstract void onBinaryFragment(ByteBuffer payload, boolean fin) throws IOException;

    public abstract void onClose(CloseInfo close);

    public abstract void onException(WebSocketException e);

    public void onFrame(Frame frame)
    {
        /* override to do something */
    }

    public abstract void onTextFragment(ByteBuffer payload, boolean fin) throws IOException;

    protected void terminateConnection(int statusCode, String rawreason)
    {
        String reason = rawreason;
        reason = StringUtil.truncate(reason,(WebSocketFrame.MAX_CONTROL_PAYLOAD - 2));
        LOG.debug("terminateConnection({},{})",statusCode,rawreason);
        session.close(statusCode,reason);
    }

    private void unhandled(Throwable t)
    {
        LOG.warn("Unhandled Error (closing connection)",t);

        // Unhandled Error, close the connection.
        switch (policy.getBehavior())
        {
            case SERVER:
                terminateConnection(StatusCode.SERVER_ERROR,t.getClass().getSimpleName());
                break;
            case CLIENT:
                terminateConnection(StatusCode.POLICY_VIOLATION,t.getClass().getSimpleName());
                break;
        }
    }
}
