package com.ossprj.commons.web.facade;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Map;

/**
 * Download and save a file to a Path
 */
public class DownloadAndSaveToFile {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void apply(final HttpClient httpClient,
                      final String url,
                      final Path filePath,
                      final Map<String, String> headers) throws IOException {

        final HttpGet get = new HttpGet(url);
        if (headers != null) {
            headers.forEach(get::addHeader);
        }
        logger.debug("Get: {}", get);

        final byte[] bytes = httpClient.execute(get, response -> EntityUtils.toByteArray(response.getEntity()));
        logger.debug("Bytes: {}", bytes);

        try (final FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(bytes);
            fos.flush();
        }

    }
}
