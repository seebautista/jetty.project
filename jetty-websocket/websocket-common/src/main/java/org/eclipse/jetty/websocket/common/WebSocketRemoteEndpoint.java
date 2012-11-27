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

package org.eclipse.jetty.websocket.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;

import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.api.extensions.OutgoingFrames;
import org.eclipse.jetty.websocket.common.message.MessageOutputStream;
import org.eclipse.jetty.websocket.common.message.MessageWriter;

/**
 * Endpoint for Writing messages to the Remote websocket.
 */
public class WebSocketRemoteEndpoint implements RemoteEndpoint
{
    private static final Logger LOG = Log.getLogger(WebSocketRemoteEndpoint.class);
    public final LogicalConnection connection;
    public final OutgoingFrames outgoing;
    public final EncoderCollection encoders;
    public MessageOutputStream stream;
    public MessageWriter writer;

    public WebSocketRemoteEndpoint(EncoderCollection encoderCollection, LogicalConnection connection, OutgoingFrames outgoing)
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("LogicalConnection cannot be null");
        }
        this.encoders = encoderCollection;
        this.connection = connection;
        this.outgoing = outgoing;
    }

    public InetSocketAddress getInetSocketAddress()
    {
        return connection.getRemoteAddress();
    }

    @Override
    public OutputStream getSendStream() throws IOException
    {
        if (isWriterActive())
        {
            throw new IOException("Cannot get OutputStream while Writer is open");
        }

        if (isStreamActive())
        {
            LOG.debug("getSendStream() -> (existing) {}",stream);
            return stream;
        }

        stream = new MessageOutputStream(connection,outgoing);
        LOG.debug("getSendStream() -> (new) {}",stream);
        return stream;
    }

    @Override
    public Writer getSendWriter() throws IOException
    {
        if (isStreamActive())
        {
            throw new IOException("Cannot get Writer while OutputStream is open");
        }

        if (isWriterActive())
        {
            LOG.debug("getSendWriter() -> (existing) {}",writer);
            return writer;
        }

        writer = new MessageWriter(connection,outgoing);
        LOG.debug("getSendWriter() -> (new) {}",writer);
        return writer;
    }

    private boolean isStreamActive()
    {
        if (stream == null)
        {
            return false;
        }

        return !stream.isClosed();
    }

    private boolean isWriterActive()
    {
        if (writer == null)
        {
            return false;
        }
        return !writer.isClosed();
    }

    /**
     * Internal
     * 
     * @param frame
     * @return
     */
    private Future<SendResult> sendAsyncFrame(WebSocketFrame frame)
    {
        try
        {
            connection.assertOutputOpen();
            return outgoing.outgoingFrame(frame);
        }
        catch (IOException e)
        {
            return new FailedFuture(e);
        }
    }

    @Override
    public void sendBytes(ByteBuffer data) throws IOException
    {
        connection.assertOutputOpen();
        if (LOG.isDebugEnabled())
        {
            LOG.debug("sendBytes({})",BufferUtil.toDetailString(data));
        }
        Frame frame = WebSocketFrame.binary().setPayload(data);
        outgoing.outgoingFrame(frame);
    }

    @Override
    public void sendBytesByCompletion(ByteBuffer data, SendHandler completion)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("sendBytesByCompletion({}, {})",BufferUtil.toDetailString(data),completion);
        }
        WebSocketFrame frame = WebSocketFrame.binary().setPayload(data);
        frame.setSendHandler(completion);
        sendAsyncFrame(frame);
    }

    @Override
    public Future<SendResult> sendBytesByFuture(ByteBuffer data)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("sendBytesByFuture({})",BufferUtil.toDetailString(data));
        }
        WebSocketFrame frame = WebSocketFrame.binary().setPayload(data);
        return sendAsyncFrame(frame);
    }

    private void sendFrame(Frame frame)
    {
        try
        {
            outgoing.outgoingFrame(frame);
        }
        catch (IOException e)
        {
            LOG.warn(e);
        }
    }

    @Override
    public void sendObject(Object o) throws IOException, EncodeException
    {
        Encoder encoder = encoders.getForObject(o);

        // TODO: encode to appropriate flow.
        // TEXT
        // BINARY
        // TEXT STREAM
        // BINARY STREAM
    }

    @Override
    public void sendObjectByCompletion(Object o, SendHandler completion)
    {
        try
        {
            Encoder encoder = encoders.getForObject(o);
            // TODO: encode to appropriate flow.
            // TEXT
            // BINARY
            // TEXT STREAM
            // BINARY STREAM
        }
        catch (EncodeException e)
        {
            completion.setResult(new SendResult(e));
        }
    }

    @Override
    public Future<SendResult> sendObjectByFuture(Object o)
    {
        try
        {
            Encoder encoder = encoders.getForObject(o);
            // TODO: encode to appropriate flow.
            // TEXT
            // BINARY
            // TEXT STREAM
            // BINARY STREAM
            return null;
        }
        catch (EncodeException e)
        {
            return new FailedFuture(e);
        }
    }

    @Override
    public void sendPartialBytes(ByteBuffer partialByte, boolean isLast) throws IOException
    {
        Frame frame = WebSocketFrame.binary().setPayload(partialByte).setFin(isLast);
        outgoing.outgoingFrame(frame);
    }

    @Override
    public void sendPartialString(String fragment, boolean isLast) throws IOException
    {
        Frame frame = WebSocketFrame.text(fragment).setFin(isLast);
        outgoing.outgoingFrame(frame);
    }

    @Override
    public void sendPing(ByteBuffer applicationData)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Ping with {}",BufferUtil.toDetailString(applicationData));
        }
        Frame frame = WebSocketFrame.ping().setPayload(applicationData);
        sendFrame(frame);
    }

    @Override
    public void sendPong(ByteBuffer applicationData)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Pong with {}",BufferUtil.toDetailString(applicationData));
        }
        Frame frame = WebSocketFrame.pong().setPayload(applicationData);
        sendFrame(frame);
    }

    @Override
    public void sendString(String text) throws IOException
    {
        Frame frame = WebSocketFrame.text(text);
        outgoing.outgoingFrame(frame);
    }

    public Future<SendResult> sendString(String text, SendHandler completion)
    {
        WebSocketFrame frame = WebSocketFrame.text(text);
        frame.setSendHandler(completion);
        return sendAsyncFrame(frame);
    }

    @Override
    public void sendStringByCompletion(String text, SendHandler completion)
    {
        WebSocketFrame frame = WebSocketFrame.text(text);
        frame.setSendHandler(completion);
        sendAsyncFrame(frame);
    }

    @Override
    public Future<SendResult> sendStringByFuture(String text)
    {
        WebSocketFrame frame = WebSocketFrame.text(text);
        return sendAsyncFrame(frame);
    }
}
