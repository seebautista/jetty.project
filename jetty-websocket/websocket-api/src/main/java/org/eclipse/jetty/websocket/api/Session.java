//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
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

import java.io.IOException;

public interface Session
{
    /**
     * Close the current conversation with a normal status code and no reason phrase.
     */
    void close() throws IOException;

    /**
     * Close the current conversation, giving a reason for the closure. Note the websocket spec defines the acceptable uses of status codes and reason phrases.
     * 
     * @param closeStatus
     *            the reason for the closure
     */
    void close(CloseStatus closeStatus) throws IOException;

    /**
     * Return the number of milliseconds before this conversation will be closed by the container if it is inactive, ie no messages are either sent or received
     * in that time.
     * 
     * @return the timeout in milliseconds.
     */
    long getIdleTimeout();

    /**
     * The maximum total length of messages, text or binary, that this Session can handle.
     * 
     * @return the message size
     */
    long getMaximumMessageSize();

    /**
     * Returns the version of the websocket protocol currently being used. This is taken as the value of the Sec-WebSocket-Version header used in the opening
     * handshake. i.e. "13".
     * 
     * @return the protocol version
     */
    String getProtocolVersion();

    /**
     * Return a reference to the RemoteEndpoint object representing the other end of this conversation.
     * 
     * @return the remote endpoint
     */
    RemoteEndpoint getRemote();

    /**
     * Get the UpgradeRequest used to create this session
     * 
     * @return the UpgradeRequest used to create this session
     */
    UpgradeRequest getUpgradeRequest();

    /**
     * Get the UpgradeResponse used to create this session
     * 
     * @return the UpgradeResponse used to create this session
     */
    UpgradeResponse getUpgradeResponse();

    /**
     * Return true if and only if the underlying socket is open.
     * 
     * @return whether the session is open
     */
    abstract boolean isOpen();

    /**
     * Return true if and only if the underlying socket is using a secure transport.
     * 
     * @return whether its using a secure transport
     */
    boolean isSecure();

    /**
     * Set the number of milliseconds before this conversation will be closed by the container if it is inactive, ie no messages are either sent or received.
     * 
     * @param ms
     *            the number of milliseconds.
     */
    void setIdleTimeout(long ms);

    /**
     * Sets the maximum total length of messages, text or binary, that this Session can handle.
     */
    void setMaximumMessageSize(long length);
}
