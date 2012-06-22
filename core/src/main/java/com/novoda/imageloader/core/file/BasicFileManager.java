/**
 * Copyright 2012 Novoda Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.novoda.imageloader.core.file;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;

import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.file.util.FileUtil;
import com.novoda.imageloader.core.network.UrlUtil;

/**
 * This is a basic implementation for the file manager.
 * On Startup it is running a cleanup of all the files in the cache, and removing
 * old images based on the expirationPeriod.
 */
public class BasicFileManager implements FileManager {

    private LoaderSettings settings;

    public BasicFileManager(LoaderSettings settings) {
        this.settings = settings;
        cleanOldFiles();
    }

    @Override
    public void clean() {
        deleteOldFiles(-1);
    }

    @Override
    public void cleanOldFiles() {
        deleteOldFiles(settings.getExpirationPeriod());
    }

    private void deleteOldFiles(final long expirationPeriod) {
        final String cacheDir = settings.getCacheDir().getAbsolutePath();
        Thread cleaner = new Thread(new Runnable() {
            public void run() {
                try {
                    new FileUtil().reduceFileCache(cacheDir, expirationPeriod);
                } catch (Throwable t) {
                    // Don't have to fail in case there
                }
            }
          });
        cleaner.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        cleaner.start();
    }

    @Override
    public String getFilePath(String imageUrl) {
        File f = getFile(imageUrl);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return null;
    }

    @Override
    public void saveBitmap(String fileName, Bitmap b, int width, int height) {
        try {
            FileOutputStream out = new FileOutputStream(fileName + "-" + width + "x" + height);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFile(String url) {
        url = processUrl(url);
        String filename = String.valueOf(url.hashCode());
        return new File(settings.getCacheDir(), filename);
    }

    @Override
    public File getFile(String url, int width, int height) {
        url = processUrl(url);
        String filename = String.valueOf(url.hashCode()) + "-" + width + "x" + height;
        return new File(settings.getCacheDir(), filename);
    }

    private String processUrl(String url) {
        if (!settings.isQueryIncludedInHash()) {
            return url;
        }
        return new UrlUtil().removeQuery(url);
    }

}
