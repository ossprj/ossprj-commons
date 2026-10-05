package com.ossprj.commons.web.facade;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadAndSaveToFileTest {

    @Test
    public void testDownloadAndSaveToFile(@TempDir Path tempDir) throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        byte[] content = "Hello HttpClient 5!".getBytes(StandardCharsets.UTF_8);

        when(response.getEntity()).thenReturn(new ByteArrayEntity(content, ContentType.TEXT_PLAIN));
        when(httpClient.execute(any(HttpGet.class), any(HttpClientResponseHandler.class))).thenAnswer(invocation -> {
            HttpClientResponseHandler<?> handler = invocation.getArgument(1);
            return handler.handleResponse(response);
        });

        Path targetFile = tempDir.resolve("downloaded.txt");
        DownloadAndSaveToFile downloadAndSaveToFile = new DownloadAndSaveToFile();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");

        downloadAndSaveToFile.apply(httpClient, "http://example.com/file", targetFile, headers);

        assertTrue(Files.exists(targetFile));
        assertArrayEquals(content, Files.readAllBytes(targetFile));
    }

    @Test
    public void testDownloadAndSaveToFileNullHeaders(@TempDir Path tempDir) throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        byte[] content = "Test Content".getBytes(StandardCharsets.UTF_8);

        when(response.getEntity()).thenReturn(new ByteArrayEntity(content, ContentType.TEXT_PLAIN));
        when(httpClient.execute(any(HttpGet.class), any(HttpClientResponseHandler.class))).thenAnswer(invocation -> {
            HttpClientResponseHandler<?> handler = invocation.getArgument(1);
            return handler.handleResponse(response);
        });

        Path targetFile = tempDir.resolve("null_headers.txt");
        DownloadAndSaveToFile downloadAndSaveToFile = new DownloadAndSaveToFile();

        downloadAndSaveToFile.apply(httpClient, "http://example.com/file", targetFile, null);

        assertTrue(Files.exists(targetFile));
        assertArrayEquals(content, Files.readAllBytes(targetFile));
    }
}
