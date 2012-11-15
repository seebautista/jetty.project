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

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.extensions.IncomingFrames;
import org.eclipse.jetty.websocket.common.WebSocketSession;

/**
 * Abstract Base Class to bridge Incoming Frames to the various drivers for frame and error events models.
 */
public abstract class AbstractEndpoint extends Endpoint implements IncomingFrames
{
    protected final Logger LOG;
    protected WebSocketSession session;
    protected WebSocketPolicy policy;

    public AbstractEndpoint(Object websocket)
    {
        LOG = Log.getLogger(websocket.getClass());
    }

    public WebSocketPolicy getPolicy()
    {
        return policy;
    }

    public WebSocketSession getSession()
    {
        return session;
    }

    public void onConnect()
    {
        this.onOpen(this.session);
    }

    public void setPolicy(WebSocketPolicy policy)
    {
        this.policy = policy;
    }

    public void setSession(WebSocketSession session)
    {
        this.session = session;
    }
}
