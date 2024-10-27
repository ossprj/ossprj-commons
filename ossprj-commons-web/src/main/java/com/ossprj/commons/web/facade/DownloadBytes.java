package com.ossprj.commons.web.facade;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class DownloadBytes {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public byte[] perform(final HttpClient httpClient,
                          final String url) throws IOException {
        return perform(httpClient, null, url);
    }

    public byte[] perform(final HttpClient httpClient,
                          final HttpContext context,
                          final String url) throws IOException {

        final HttpGet get = new HttpGet(url);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final HttpResponse response = httpClient.execute(get, context);
        final HttpEntity entity = response.getEntity();

        logger.debug("response: {} size: {} type: {}", response.getStatusLine(), entity.getContentLength(), entity.getContentType().getValue());

        if (response.getStatusLine().getStatusCode() != 200) {
            EntityUtils.consume(entity);
            throw new IllegalStateException("status code != 200");
        }

        baos.write(EntityUtils.toByteArray(entity));
        baos.flush();
        return baos.toByteArray();
    }
}
