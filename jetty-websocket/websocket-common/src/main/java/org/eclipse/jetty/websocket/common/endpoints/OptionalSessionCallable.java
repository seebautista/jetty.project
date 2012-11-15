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

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;

import org.eclipse.jetty.websocket.api.WebSocketConnection;
import org.eclipse.jetty.websocket.common.WebSocketSession;

/**
 * Simple Callable that manages the optional {@link WebSocketSession} argument
 */
public class OptionalSessionCallable extends Callable
{
    private final boolean wantsConnection;
    private final boolean streaming;

    public OptionalSessionCallable(Class<?> pojo, Method method)
    {
        super(pojo,method);

        boolean foundConnection = false;
        boolean foundStreaming = false;

        if (paramTypes != null)
        {
            for (Class<?> paramType : paramTypes)
            {
                if (WebSocketConnection.class.isAssignableFrom(paramType))
                {
                    foundConnection = true;
                }
                if (Reader.class.isAssignableFrom(paramType) || InputStream.class.isAssignableFrom(paramType))
                {
                    foundStreaming = true;
                }
            }
        }

        this.wantsConnection = foundConnection;
        this.streaming = foundStreaming;
    }

    public void call(Object obj, WebSocketConnection connection, Object... args)
    {
        if (wantsConnection)
        {
            Object fullArgs[] = new Object[args.length + 1];
            fullArgs[0] = connection;
            System.arraycopy(args,0,fullArgs,1,args.length);
            call(obj,fullArgs);
        }
        else
        {
            call(obj,args);
        }
    }

    public boolean isConnectionAware()
    {
        return wantsConnection;
    }

    @Override
    public boolean isStreaming()
    {
        return streaming;
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]",this.getClass().getSimpleName(),method.toGenericString());
    }
}
