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

import javax.websocket.EndpointConfiguration;
import javax.websocket.Session;
import javax.websocket.WebSocketEndpoint;

import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.extensions.Frame;

/**
 * Endpoint for POJOs annotated using <code>javax.websocket</code> {@link WebSocketEndpoint &#064;WebSocketEndpoint}
 */
public class JavaxPojoEndpoint extends AbstractEndpoint
{
    private final Object websocket;
    private final JavaxPojoMetadata metadata;
    private AnnotatedEndpointConfiguration endpointConfiguration;

    public JavaxPojoEndpoint(Object websocket)
    {
        super(websocket);
        this.websocket = websocket;
        this.metadata = JavaxPojoAnnotationCache.discover(websocket);

        WebSocketEndpoint aendpoint = websocket.getClass().getAnnotation(WebSocketEndpoint.class);
        if (aendpoint == null)
        {
            throw new WebSocketException("Not a valid @WebSocketEndpoint");
        }

        // Establish EndpointConfiguration
        endpointConfiguration = new AnnotatedEndpointConfiguration(aendpoint);

        // TODO Discover @WebSocketClose [0..1]

        // TODO Discover @WebSocketOpen [0..1]
        // TODO warn on no open found

        // TODO Discover @WebSocketError [0..1]

        // TODO Discover @WebSocketMessage [0..n]
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration()
    {
        return endpointConfiguration;
    }

    @Override
    public void incomingError(WebSocketException e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void incomingFrame(Frame frame)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOpen(Session session)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]", this.getClass().getSimpleName(), websocket);
    }
}
