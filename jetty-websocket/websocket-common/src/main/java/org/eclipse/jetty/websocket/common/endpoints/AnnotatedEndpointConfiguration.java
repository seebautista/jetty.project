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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.websocket.ClientEndpointConfiguration;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfiguration;
import javax.websocket.WebSocketEndpoint;

import org.eclipse.jetty.websocket.api.WebSocketException;

/**
 * {@link EndpointConfiguration} arriving via {@link javax.websocket.WebSocketEndpoint} annotation.
 */
public class AnnotatedEndpointConfiguration implements ClientEndpointConfiguration
{
    private final List<Decoder> decoders;
    private final List<Encoder> encoders;
    private final List<String> subprotocols;
    private final String path;

    public AnnotatedEndpointConfiguration(WebSocketEndpoint endpoint)
    {
        decoders = new ArrayList<>();
        encoders = new ArrayList<>();
        subprotocols = new ArrayList<>();

        path = endpoint.value();
        if (hasEntries(endpoint.subprotocols()))
        {
            subprotocols.addAll(Arrays.asList(endpoint.subprotocols()));
        }

        initDecoders(endpoint.decoders());
        initEncoders(endpoint.encoders());
    }

    @Override
    public List<Decoder> getDecoders()
    {
        return decoders;
    }

    @Override
    public List<Encoder> getEncoders()
    {
        return encoders;
    }

    @Override
    public List<String> getExtensions()
    {
        return null; // always null in this situation
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public List<String> getPreferredSubprotocols()
    {
        return subprotocols;
    }

    private <T> boolean hasEntries(T entries[])
    {
        return (entries != null) && (entries.length > 0);
    }

    private final void initDecoders(Class<? extends Decoder>[] decoderClasses)
    {
        if (!hasEntries(decoderClasses))
        {
            return; // nothing to do
        }
        // Initialize Decoders and add to list.
        for (Class<? extends Decoder> decoderClass : decoderClasses)
        {
            try
            {
                Decoder decoder = decoderClass.newInstance();
                this.decoders.add(decoder);
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new WebSocketException("Unable to initialize Decoder: " + decoderClass,e);
            }
        }
    }

    private void initEncoders(Class<? extends Encoder>[] encodersClasses)
    {
        if (!hasEntries(encodersClasses))
        {
            return; // nothing to do
        }
        // Initialize Encoder and add to list.
        for (Class<? extends Encoder> encoderClass : encodersClasses)
        {
            try
            {
                Encoder decoder = encoderClass.newInstance();
                this.encoders.add(decoder);
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new WebSocketException("Unable to initialize Encoder: " + encoderClass,e);
            }
        }
    }
}
