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

package org.eclipse.jetty.websocket.api;

/**
 * Settings for WebSocket operations.
 */
public class WebSocketPolicy
{
    public static WebSocketPolicy newClientPolicy()
    {
        return new WebSocketPolicy(WebSocketBehavior.CLIENT);
    }

    public static WebSocketPolicy newServerPolicy()
    {
        return new WebSocketPolicy(WebSocketBehavior.SERVER);
    }

    /**
     * The maximum allowed payload size (validated in both directions)
     * <p>
     * Default: 65536 (64K)
     */
    private int maxPayloadSize = 65536;

    /**
     * The maximum size of a message (text or binary) allowed
     * <p>
     * Default: 16384 (16 K)
     */
    private long maxMessageSize = 16384;

    /**
     * Maximum Message Buffer size, which is also the max frame byte size.
     * <p>
     * Default: 65536 (64 K)
     */
    private int bufferSize = 65536;

    // TODO: change bufferSize to windowSize for FrameBytes logic?

    /**
     * The time in ms (milliseconds) that a websocket may be idle before closing.
     * <p>
     * Default: 300000 (ms)
     */
    private long idleTimeout = 300000;

    /**
     * Behavior of the websockets
     */
    private final WebSocketBehavior behavior;

    public WebSocketPolicy(WebSocketBehavior behavior)
    {
        this.behavior = behavior;
    }

    public void assertValidMessageSize(int requestedSize)
    {
        // validate it
        if (requestedSize > maxMessageSize)
        {
            throw new MessageTooLargeException("Requested message size [" + requestedSize + "] exceeds maximum size [" + maxMessageSize + "]");
        }
    }

    public void assertValidPayloadLength(int payloadLength)
    {
        // validate to buffer sizes
        if (payloadLength > maxPayloadSize)
        {
            throw new MessageTooLargeException("Requested payload length [" + payloadLength + "] exceeds maximum size [" + maxPayloadSize + "]");
        }
    }

    public WebSocketPolicy clonePolicy()
    {
        WebSocketPolicy clone = new WebSocketPolicy(this.behavior);
        clone.idleTimeout = this.idleTimeout;
        clone.bufferSize = this.bufferSize;
        clone.maxPayloadSize = this.maxPayloadSize;
        clone.maxMessageSize = this.maxMessageSize;
        return clone;
    }

    public WebSocketBehavior getBehavior()
    {
        return behavior;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public long getIdleTimeout()
    {
        return idleTimeout;
    }

    public long getMaxMessageSize()
    {
        return maxMessageSize;
    }

    public int getMaxPayloadSize()
    {
        return maxPayloadSize;
    }

    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public void setIdleTimeout(long idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }

    public void setMaxMessageSize(long maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        if (maxMessageSize <= 0)
        {
            throw new IllegalArgumentException("Max Message Size must be 1 byte or larger");
        }
    }

    public void setMaxPayloadSize(int maxPayloadSize)
    {
        if (maxPayloadSize < bufferSize)
        {
            throw new IllegalStateException("Cannot have payload size be smaller than buffer size");
        }
        this.maxPayloadSize = maxPayloadSize;
    }
}
