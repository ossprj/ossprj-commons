package com.ossprj.commons.web.facade;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
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

        return httpClient.execute(get, context, response -> {
            final HttpEntity entity = response.getEntity();

            logger.debug("response: {} size: {} type: {}", response.getCode(), entity != null ? entity.getContentLength() : -1, entity != null ? entity.getContentType() : null);

            if (response.getCode() != 200) {
                EntityUtils.consume(entity);
                throw new IllegalStateException("status code != 200");
            }

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (entity != null) {
                baos.write(EntityUtils.toByteArray(entity));
            }
            baos.flush();
            return baos.toByteArray();
        });
    }
}
