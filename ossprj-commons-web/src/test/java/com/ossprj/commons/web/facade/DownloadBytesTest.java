package com.ossprj.commons.web.facade;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadBytesTest {

    @Test
    public void testPerformSuccess() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        byte[] expectedBytes = "Response Payload".getBytes(StandardCharsets.UTF_8);

        when(response.getCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(new ByteArrayEntity(expectedBytes, ContentType.APPLICATION_OCTET_STREAM));

        when(httpClient.execute(any(HttpGet.class), eq(null), any(HttpClientResponseHandler.class))).thenAnswer(invocation -> {
            HttpClientResponseHandler<?> handler = invocation.getArgument(2);
            return handler.handleResponse(response);
        });

        DownloadBytes downloadBytes = new DownloadBytes();
        byte[] result = downloadBytes.perform(httpClient, "http://example.com/data");

        assertArrayEquals(expectedBytes, result);
    }

    @Test
    public void testPerformWithContextSuccess() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpContext httpContext = mock(HttpContext.class);
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        byte[] expectedBytes = "Response With Context".getBytes(StandardCharsets.UTF_8);

        when(response.getCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(new ByteArrayEntity(expectedBytes, ContentType.APPLICATION_OCTET_STREAM));

        when(httpClient.execute(any(HttpGet.class), eq(httpContext), any(HttpClientResponseHandler.class))).thenAnswer(invocation -> {
            HttpClientResponseHandler<?> handler = invocation.getArgument(2);
            return handler.handleResponse(response);
        });

        DownloadBytes downloadBytes = new DownloadBytes();
        byte[] result = downloadBytes.perform(httpClient, httpContext, "http://example.com/data");

        assertArrayEquals(expectedBytes, result);
    }

    @Test
    public void testPerformNon200ThrowsException() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);

        when(response.getCode()).thenReturn(404);
        when(response.getEntity()).thenReturn(new ByteArrayEntity("Not Found".getBytes(StandardCharsets.UTF_8), ContentType.TEXT_PLAIN));

        when(httpClient.execute(any(HttpGet.class), eq(null), any(HttpClientResponseHandler.class))).thenAnswer(invocation -> {
            HttpClientResponseHandler<?> handler = invocation.getArgument(2);
            return handler.handleResponse(response);
        });

        DownloadBytes downloadBytes = new DownloadBytes();
        assertThrows(IllegalStateException.class, () -> downloadBytes.perform(httpClient, "http://example.com/notfound"));
    }
}
