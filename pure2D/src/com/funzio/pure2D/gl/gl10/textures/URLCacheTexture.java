/**
 * 
 */
package com.funzio.pure2D.gl.gl10.textures;

import java.io.File;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.funzio.pure2D.gl.gl10.GLState;
import com.funzio.pure2D.loaders.tasks.DownloadTask;
import com.funzio.pure2D.utils.Pure2DUtils;

/**
 * @author long
 */
public class URLCacheTexture extends Texture {
    private String mFileUrl;
    private String mCachePath;
    private TextureOptions mOptions;
    private boolean mIsAsync = false;

    protected URLCacheTexture(final GLState glState, final String fileUrl, final String cachePath, final TextureOptions options) {
        super(glState);

        load(fileUrl, cachePath, options);
    }

    protected URLCacheTexture(final GLState glState, final String fileUrl, final String cachePath, final TextureOptions options, final boolean async) {
        super(glState);

        if (async) {
            loadAsync(fileUrl, cachePath, options);
        } else {
            load(fileUrl, cachePath, options);
        }
    }

    public void load(final String fileUrl, final String cachePath, final TextureOptions options) {
        mIsAsync = false;
        mFileUrl = fileUrl;
        mCachePath = cachePath;
        // mFilePath = filePath;
        mOptions = options;

        int[] dimensions = new int[2];
        Bitmap bitmap = null;

        final File file = new File(mCachePath);
        if (file.exists()) {
            bitmap = Pure2DUtils.getFileBitmap(mCachePath, options, dimensions);
        } else if (fileUrl != null && fileUrl.length() > 0) {
            // try to download and cache
            if (new DownloadTask(mFileUrl, mCachePath).run()) {
                bitmap = Pure2DUtils.getFileBitmap(mCachePath, options, dimensions);
            }
        }

        if (bitmap != null) {
            load(bitmap, dimensions[0], dimensions[1], options != null ? options.inMipmaps : 0);
            bitmap.recycle();
        } else {
            Log.e(TAG, "Unable to load bitmap: " + mCachePath, new Exception());
            // callback, regardless whether it's successful or not
            if (mListener != null) {
                mListener.onTextureLoad(this);
            }
        }
    }

    @Override
    public void reload() {
        if (mIsAsync) {
            loadAsync(mFileUrl, mCachePath, mOptions);
        } else {
            load(mFileUrl, mCachePath, mOptions);
        }
    }

    /**
     * Load Asynchronously without block GL thread.
     * 
     * @param filePath
     * @param options
     * @param po2
     */
    @SuppressLint("NewApi")
    public void loadAsync(final String fileUrl, final String cachePath, final TextureOptions options) {
        mIsAsync = true;
        mFileUrl = fileUrl;
        mCachePath = cachePath;
        mOptions = options;

        // AsyncTask can only be initialized on UI Thread, especially on Android 2.2
        mGLState.getStage().getHandler().post(new Runnable() {
            @Override
            public void run() {
                final AsyncLoader loader = new AsyncLoader();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    loader.execute();
                }
            }
        });

    }

    private class AsyncLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(final Void... params) {
            final int[] dimensions = new int[2];
            Bitmap bitmap = null;
            final File file = new File(mCachePath);
            if (file.exists()) {
                bitmap = Pure2DUtils.getFileBitmap(mCachePath, mOptions, dimensions);
            } else if (mFileUrl != null && mFileUrl.length() > 0) {
                // try to download and cache
                if (new DownloadTask(mFileUrl, mCachePath).run()) {
                    bitmap = Pure2DUtils.getFileBitmap(mCachePath, mOptions, dimensions);
                }
            }
            final Bitmap finalBitmap = bitmap;

            mGLState.queueEvent(new Runnable() {

                @Override
                public void run() {
                    if (finalBitmap != null) {
                        load(finalBitmap, dimensions[0], dimensions[1], mOptions != null ? mOptions.inMipmaps : 0);
                        finalBitmap.recycle();
                    } else {
                        Log.e(TAG, "Unable to load bitmap: " + mCachePath, new Exception());
                        // callback, regardless whether it's successful or not
                        if (mListener != null) {
                            mListener.onTextureLoad(URLCacheTexture.this);
                        }
                    }
                }
            });

            return null;
        }
    }

    @Override
    public String toString() {
        return mFileUrl;
    }
}
