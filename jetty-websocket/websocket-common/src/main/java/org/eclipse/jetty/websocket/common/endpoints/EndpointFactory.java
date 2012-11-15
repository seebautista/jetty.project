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

import java.lang.annotation.Annotation;

import javax.websocket.Endpoint;
import javax.websocket.WebSocketEndpoint;

import org.eclipse.jetty.websocket.api.InvalidWebSocketException;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Central place to obtain AbstractEndpoint implementations.
 */
public class EndpointFactory
{
    public static AbstractEndpoint create(Object websocket)
    {
        if (websocket == null)
        {
            throw new InvalidWebSocketException("null websocket object");
        }

        // [jetty] extending WebSocketListener
        if (websocket instanceof WebSocketListener)
        {
            WebSocketListener listener = (WebSocketListener)websocket;
            return new JettyEndpoint(listener);
        }

        // [jetty] annotated with @WebSocket
        if(hasAnnotation(websocket, WebSocket.class))
        {
            return new JettyPojoEndpoint(websocket);
        }

        // [jsr-356] extending javax.websocket.Endpoint
        if (websocket instanceof Endpoint)
        {
            Endpoint endpoint = (Endpoint)websocket;
            return new JavaxEndpoint(endpoint);
        }

        // [jsr-356] annotated with @WebSocketEndpoint
        if(hasAnnotation(websocket,WebSocketEndpoint.class))
        {
            return new JavaxPojoEndpoint(websocket);
        }

        // Create a clear error message for the developer
        StringBuilder err = new StringBuilder();
        err.append(websocket.getClass().getName());
        err.append(" is not a valid WebSocket object.");
        err.append("  Object must obey one of the following rules: ");
        err.append(" (1) extend from ").append(Endpoint.class.getName());
        err.append(" or (2) annotated with @").append(WebSocketEndpoint.class.getName());
        err.append(" or (3) implement ").append(WebSocketListener.class.getName());
        err.append(" or (4) annotated with @").append(WebSocket.class.getName());
        throw new InvalidWebSocketException(err.toString());
    }

    private static boolean hasAnnotation(Object websocket, Class<? extends Annotation> annotationClass)
    {
        return websocket.getClass().getAnnotation(annotationClass) != null;
    }
}
