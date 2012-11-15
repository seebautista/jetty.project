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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketConnection;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;

/**
 * Cache for discovered Jetty {@link WebSocket &#064;WebSocket} annotated websockets
 */
public class JettyPojoAnnotationCache extends AbstractMethodAnnotationScanner<JettyPojoMetadata>
{
    private static final Logger LOG = Log.getLogger(JettyPojoAnnotationCache.class);
    public static final JettyPojoAnnotationCache INSTANCE = new JettyPojoAnnotationCache();

    /**
     * Parameter list for &#064;OnWebSocketMessage (Binary mode)
     */
    private static final ParamList validBinaryParams;

    /**
     * Parameter list for &#064;OnWebSocketConnect
     */
    private static final ParamList validConnectParams;

    /**
     * Parameter list for &#064;OnWebSocketClose
     */
    private static final ParamList validCloseParams;

    /**
     * Parameter list for &#064;OnWebSocketFrame
     */
    private static final ParamList validFrameParams;

    /**
     * Parameter list for &#064;OnWebSocketMessage (Text mode)
     */
    private static final ParamList validTextParams;

    static
    {
        validConnectParams = new ParamList();
        validConnectParams.addParams(WebSocketConnection.class);

        validCloseParams = new ParamList();
        validCloseParams.addParams(int.class,String.class);
        validCloseParams.addParams(WebSocketConnection.class,int.class,String.class);

        validTextParams = new ParamList();
        validTextParams.addParams(String.class);
        validTextParams.addParams(WebSocketConnection.class,String.class);
        validTextParams.addParams(Reader.class);
        validTextParams.addParams(WebSocketConnection.class,Reader.class);

        validBinaryParams = new ParamList();
        validBinaryParams.addParams(byte[].class,int.class,int.class);
        validBinaryParams.addParams(WebSocketConnection.class,byte[].class,int.class,int.class);
        validBinaryParams.addParams(InputStream.class);
        validBinaryParams.addParams(WebSocketConnection.class,InputStream.class);

        validFrameParams = new ParamList();
        validFrameParams.addParams(Frame.class);
        validFrameParams.addParams(WebSocketConnection.class,Frame.class);
    }

    public synchronized static JettyPojoMetadata discover(Class<?> websocket)
    {
        WebSocket anno = websocket.getAnnotation(WebSocket.class);
        if (anno == null)
        {
            return null;
        }

        JettyPojoMetadata metadata = INSTANCE.cache.get(websocket);
        if (metadata == null)
        {
            metadata = new JettyPojoMetadata();
            INSTANCE.scanMethodAnnotations(metadata,websocket);
            INSTANCE.cache.put(websocket,metadata);
        }

        return metadata;
    }

    public static JettyPojoMetadata discover(Object websocket)
    {
        return discover(websocket.getClass());
    }

    private ConcurrentHashMap<Class<?>, JettyPojoMetadata> cache;

    public JettyPojoAnnotationCache()
    {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void onMethodAnnotation(JettyPojoMetadata metadata, Class<?> pojo, Method method, Annotation annotation)
    {
        LOG.debug("onMethodAnnotation({}, {}, {}, {})",metadata,pojo,method,annotation);

        if (isAnnotation(annotation,OnWebSocketConnect.class))
        {
            assertValidSignature(method,OnWebSocketConnect.class,validConnectParams);
            assertUnset(metadata.onConnect,OnWebSocketConnect.class,method);
            metadata.onConnect = new Callable(pojo,method);
            return;
        }

        if (isAnnotation(annotation,OnWebSocketMessage.class))
        {
            if (isSignatureMatch(method,validTextParams))
            {
                // Text mode
                assertUnset(metadata.onText,OnWebSocketMessage.class,method);
                metadata.onText = new OptionalSessionCallable(pojo,method);
                return;
            }

            if (isSignatureMatch(method,validBinaryParams))
            {
                // Binary Mode
                assertUnset(metadata.onBinary,OnWebSocketMessage.class,method);
                metadata.onBinary = new OptionalSessionCallable(pojo,method);
                return;
            }

            throw InvalidSignatureException.build(method,OnWebSocketMessage.class,validTextParams,validBinaryParams);
        }

        if (isAnnotation(annotation,OnWebSocketClose.class))
        {
            assertValidSignature(method,OnWebSocketClose.class,validCloseParams);
            assertUnset(metadata.onClose,OnWebSocketClose.class,method);
            metadata.onClose = new OptionalSessionCallable(pojo,method);
            return;
        }

        if (isAnnotation(annotation,OnWebSocketFrame.class))
        {
            assertValidSignature(method,OnWebSocketFrame.class,validFrameParams);
            assertUnset(metadata.onFrame,OnWebSocketFrame.class,method);
            metadata.onFrame = new OptionalSessionCallable(pojo,method);
            return;
        }
    }
}
