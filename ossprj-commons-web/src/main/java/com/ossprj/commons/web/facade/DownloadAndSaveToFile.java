package com.ossprj.commons.web.facade;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
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
        headers.forEach(get::addHeader);
        logger.debug("Get: {}", get);

        final byte[] bytes = EntityUtils.toByteArray(httpClient.execute(get).getEntity());
        logger.debug("Bytes: {}", bytes);

        final FileOutputStream fos = new FileOutputStream(filePath.toFile());
        fos.write(bytes);
        fos.flush();

    }
}
