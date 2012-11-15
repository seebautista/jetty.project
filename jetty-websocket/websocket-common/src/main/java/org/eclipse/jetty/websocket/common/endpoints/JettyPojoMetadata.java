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

/**
 * Methods that may exist for Jetty's annotated POJOs
 */
public class JettyPojoMetadata
{
    /** &#064;OnWebSocketConnect () */
    public Callable onConnect;
    /** &#064;OnWebSocketMessage (byte[], or ByteBuffer, or InputStream) */
    public OptionalSessionCallable onBinary;
    /** &#064;OnWebSocketMessage (String, or Reader) */
    public OptionalSessionCallable onText;
    /** &#064;OnWebSocketFrame (Frame) */
    public OptionalSessionCallable onFrame;
    /** &#064;OnWebSocketError (Throwable) */
    public OptionalSessionCallable onException;
    /** &#064;OnWebSocketClose (Frame) */
    public OptionalSessionCallable onClose;

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append("JettyPojoMetadata[");
        s.append("onConnect=").append(onConnect);
        s.append(",onBinary=").append(onBinary);
        s.append(",onText=").append(onText);
        s.append(",onFrame=").append(onFrame);
        s.append(",onException=").append(onException);
        s.append(",onClose=").append(onClose);
        s.append("]");
        return s.toString();
    }
}
