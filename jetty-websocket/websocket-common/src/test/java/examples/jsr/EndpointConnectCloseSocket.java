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

package examples.jsr;

import javax.websocket.CloseReason;
import javax.websocket.DefaultServerConfiguration;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfiguration;
import javax.websocket.Session;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.common.endpoints.EventCapture;

public class EndpointConnectCloseSocket extends Endpoint
{
    private static final Logger LOG = Log.getLogger(EndpointConnectCloseSocket.class);
    public EventCapture capture = new EventCapture();

    @Override
    public EndpointConfiguration getEndpointConfiguration()
    {
        return new DefaultServerConfiguration("/test");
    }

    @Override
    public void onClose(CloseReason closeReason)
    {
        capture.add("onClose([%d,%s])",closeReason.getCloseCode().getCode(),closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Throwable thr)
    {
        LOG.debug(thr);
        capture.add("onError((%s) %s)", thr.getClass().getName(), thr.getMessage());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onOpen(Session session)
    {
        capture.add("onOpen(%s)",session);
    }
}
