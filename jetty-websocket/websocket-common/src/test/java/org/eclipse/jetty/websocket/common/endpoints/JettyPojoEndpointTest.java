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

import static org.hamcrest.Matchers.*;

import org.eclipse.jetty.websocket.api.InvalidWebSocketException;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.CloseInfo;
import org.eclipse.jetty.websocket.common.OpCode;
import org.eclipse.jetty.websocket.common.WebSocketFrame;
import org.eclipse.jetty.websocket.common.annotations.BadBinarySignatureSocket;
import org.eclipse.jetty.websocket.common.annotations.BadDuplicateFrameSocket;
import org.eclipse.jetty.websocket.common.annotations.BadTextSignatureSocket;
import org.eclipse.jetty.websocket.common.annotations.FrameSocket;
import org.eclipse.jetty.websocket.common.annotations.MyEchoBinarySocket;
import org.eclipse.jetty.websocket.common.annotations.MyEchoSocket;
import org.eclipse.jetty.websocket.common.annotations.MyStatelessEchoSocket;
import org.eclipse.jetty.websocket.common.annotations.NoopSocket;
import org.eclipse.jetty.websocket.common.io.LocalWebSocketSession;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import examples.AnnotatedBinaryArraySocket;
import examples.AnnotatedBinaryStreamSocket;
import examples.AnnotatedFramesSocket;
import examples.AnnotatedTextSocket;
import examples.AnnotatedTextStreamSocket;

/**
 * Tests for Jetty Annotated WebSockets
 */
public class JettyPojoEndpointTest
{
    @Rule
    public TestName testname = new TestName();

    private void assertHasCallable(String message, Callable actual)
    {
        Assert.assertThat(message + " Callable",actual,notNullValue());

        Assert.assertThat(message + " Callable.pojo",actual.pojo,notNullValue());
        Assert.assertThat(message + " Callable.method",actual.method,notNullValue());
    }

    private void assertNoCallable(String message, Callable actual)
    {
        Assert.assertThat(message + " Callable",actual,nullValue());
    }

    private AbstractEndpoint createEndpoint(Object websocket)
    {
        AbstractEndpoint endpoint = new JettyPojoEndpoint(websocket);
        endpoint.setPolicy(WebSocketPolicy.newClientPolicy());
        return endpoint;
    }

    /**
     * Test Case for bad declaration (duplicate frame type methods)
     */
    @Test
    public void testAnnotatedBadDuplicateFrameSocket()
    {
        try
        {
            // Should toss exception
            new JettyPojoEndpoint(new BadDuplicateFrameSocket());
            Assert.fail("Should have thrown " + InvalidWebSocketException.class);
        }
        catch (InvalidWebSocketException e)
        {
            // Validate that we have clear error message to the developer
            Assert.assertThat(e.getMessage(),containsString("Duplicate @OnWebSocketFrame"));
        }
    }

    /**
     * Test Case for bad declaration a method with a non-void return type
     */
    @Test
    public void testAnnotatedBadSignature_NonVoidReturn()
    {
        try
        {
            // Should toss exception
            new JettyPojoEndpoint(new BadBinarySignatureSocket());
            Assert.fail("Should have thrown " + InvalidWebSocketException.class);
        }
        catch (InvalidWebSocketException e)
        {
            // Validate that we have clear error message to the developer
            Assert.assertThat(e.getMessage(),containsString("must be void"));
        }
    }

    /**
     * Test Case for bad declaration a method with a public static method
     */
    @Test
    public void testAnnotatedBadSignature_Static()
    {
        try
        {
            // Should toss exception
            new JettyPojoEndpoint(new BadTextSignatureSocket());
            Assert.fail("Should have thrown " + InvalidWebSocketException.class);
        }
        catch (InvalidWebSocketException e)
        {
            // Validate that we have clear error message to the developer
            Assert.assertThat(e.getMessage(),containsString("may not be static"));
        }
    }

    /**
     * Test Case for socket for binary array messages
     */
    @Test
    public void testAnnotatedBinaryArraySocket()
    {
        Object websocket = new AnnotatedBinaryArraySocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = AnnotatedBinaryArraySocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertHasCallable(classId + ".onBinary",metadata.onBinary);
        assertHasCallable(classId + ".onClose",metadata.onClose);
        assertHasCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertNoCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);

        Assert.assertFalse(classId + ".onBinary.isConnectionAware",metadata.onBinary.isConnectionAware());
        Assert.assertFalse(classId + ".onBinary.isStreaming",metadata.onBinary.isStreaming());
    }

    /**
     * Test Case for socket for binary stream messages
     */
    @Test
    public void testAnnotatedBinaryStreamSocket()
    {
        Object websocket = new AnnotatedBinaryStreamSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = AnnotatedBinaryStreamSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertHasCallable(classId + ".onBinary",metadata.onBinary);
        assertHasCallable(classId + ".onClose",metadata.onClose);
        assertHasCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertNoCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);

        Assert.assertFalse(classId + ".onBinary.isConnectionAware",metadata.onBinary.isConnectionAware());
        Assert.assertTrue(classId + ".onBinary.isStreaming",metadata.onBinary.isStreaming());
    }

    /**
     * Test Case for no exceptions and 4 methods (3 methods from parent)
     */
    @Test
    public void testAnnotatedMyEchoBinarySocket()
    {
        Object websocket = new MyEchoBinarySocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = MyEchoBinarySocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertHasCallable(classId + ".onBinary",metadata.onBinary);
        assertHasCallable(classId + ".onClose",metadata.onClose);
        assertHasCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertHasCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);
    }

    /**
     * Test Case for no exceptions and 3 methods
     */
    @Test
    public void testAnnotatedMyEchoSocket()
    {
        Object websocket = new MyEchoSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = MyEchoSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertNoCallable(classId + ".onBinary",metadata.onBinary);
        assertHasCallable(classId + ".onClose",metadata.onClose);
        assertHasCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertHasCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);
    }

    /**
     * Test Case for annotated for text messages w/connection param
     */
    @Test
    public void testAnnotatedMyStatelessEchoSocket()
    {
        Object websocket = new MyStatelessEchoSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = MyStatelessEchoSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertNoCallable(classId + ".onBinary",metadata.onBinary);
        assertNoCallable(classId + ".onClose",metadata.onClose);
        assertNoCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertHasCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);

        Assert.assertTrue(classId + ".onText.isConnectionAware",metadata.onText.isConnectionAware());
        Assert.assertFalse(classId + ".onText.isStreaming",metadata.onText.isStreaming());
    }

    /**
     * Test Case for no exceptions and no methods
     */
    @Test
    public void testAnnotatedNoop()
    {
        Object websocket = new NoopSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = NoopSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertNoCallable(classId + ".onBinary",metadata.onBinary);
        assertNoCallable(classId + ".onClose",metadata.onClose);
        assertNoCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertNoCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);
    }

    /**
     * Test Case for no exceptions and 1 methods
     */
    @Test
    public void testAnnotatedOnFrame()
    {
        Object websocket = new FrameSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = FrameSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertNoCallable(classId + ".onBinary",metadata.onBinary);
        assertNoCallable(classId + ".onClose",metadata.onClose);
        assertNoCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertNoCallable(classId + ".onText",metadata.onText);
        assertHasCallable(classId + ".onFrame",metadata.onFrame);
    }

    /**
     * Test Case for socket for simple text messages
     */
    @Test
    public void testAnnotatedTextSocket()
    {
        Object websocket = new AnnotatedTextSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = AnnotatedTextSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertNoCallable(classId + ".onBinary",metadata.onBinary);
        assertHasCallable(classId + ".onClose",metadata.onClose);
        assertHasCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertHasCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);

        Assert.assertFalse(classId + ".onText.isConnectionAware",metadata.onText.isConnectionAware());
        Assert.assertFalse(classId + ".onText.isStreaming",metadata.onText.isStreaming());
    }

    /**
     * Test Case for socket for text stream messages
     */
    @Test
    public void testAnnotatedTextStreamSocket()
    {
        Object websocket = new AnnotatedTextStreamSocket();
        JettyPojoEndpoint endpoint = new JettyPojoEndpoint(websocket);
        JettyPojoMetadata metadata = endpoint.getMetadata();

        String classId = AnnotatedTextStreamSocket.class.getSimpleName();

        Assert.assertThat("JettyPojoMetadata for " + classId,metadata,notNullValue());

        assertNoCallable(classId + ".onBinary",metadata.onBinary);
        assertHasCallable(classId + ".onClose",metadata.onClose);
        assertHasCallable(classId + ".onConnect",metadata.onConnect);
        assertNoCallable(classId + ".onException",metadata.onException);
        assertHasCallable(classId + ".onText",metadata.onText);
        assertNoCallable(classId + ".onFrame",metadata.onFrame);

        Assert.assertFalse(classId + ".onText.isConnectionAware",metadata.onText.isConnectionAware());
        Assert.assertTrue(classId + ".onText.isStreaming",metadata.onText.isStreaming());
    }

    @Test
    public void testEvents_AnnotatedByteArray()
    {
        AnnotatedBinaryArraySocket socket = new AnnotatedBinaryArraySocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(WebSocketFrame.binary().setPayload("Hello World"));
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onConnect");
        socket.capture.assertEvent(1,"onBinary([11],0,11)");
        socket.capture.assertEventStartsWith(2,"onClose(1000,");
    }

    @Test
    public void testEvents_AnnotatedFrames()
    {
        AnnotatedFramesSocket socket = new AnnotatedFramesSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(new WebSocketFrame(OpCode.PING).setPayload("PING"));
        endpoint.incomingFrame(WebSocketFrame.text("Text Me"));
        endpoint.incomingFrame(WebSocketFrame.binary().setPayload("Hello Bin"));
        endpoint.incomingFrame(new CloseInfo(StatusCode.SHUTDOWN).asFrame());

        socket.capture.assertEventCount(6);
        socket.capture.assertEventStartsWith(0,"onConnect(");
        socket.capture.assertEventStartsWith(1,"onFrame(PING[");
        socket.capture.assertEventStartsWith(2,"onFrame(TEXT[");
        socket.capture.assertEventStartsWith(3,"onFrame(BINARY[");
        socket.capture.assertEventStartsWith(4,"onFrame(CLOSE[");
        socket.capture.assertEventStartsWith(5,"onClose(1001,");
    }

    @Test
    public void testEvents_AnnotatedInputStream()
    {
        AnnotatedBinaryStreamSocket socket = new AnnotatedBinaryStreamSocket();
        AbstractEndpoint endpoint = createEndpoint(socket);

        LocalWebSocketSession conn = new LocalWebSocketSession(testname,endpoint);
        conn.setPolicy(endpoint.getPolicy());
        conn.open();
        endpoint.incomingFrame(WebSocketFrame.binary().setPayload("Hello World"));
        endpoint.incomingFrame(new CloseInfo(StatusCode.NORMAL).asFrame());

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onConnect");
        socket.capture.assertEventRegex(1,"^onBinary\\(.*InputStream.*");
        socket.capture.assertEventStartsWith(2,"onClose(1000,");
    }
}
