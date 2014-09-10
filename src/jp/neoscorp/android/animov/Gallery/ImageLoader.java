package jp.neoscorp.android.animov.Gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.neoscorp.android.animov.Gallery.GalleryAdapter.GalleryViewHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
    private static final String TAG = ImageLoader.class.getSimpleName();
    private final MemoryCache memoryCache = new MemoryCache();
    private final FileCache fileCache;
    private final Map<ImageView, String> videoMap =
            Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private final ExecutorService executorService;
    private final Handler handler = new Handler(); // handler to display images in UI thread
    private final Context loaderContext;
    private final Map<String, Integer> videoIdNameMap;

    public ImageLoader(Context context, Map<String, Integer> vidIdNameMap) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(1);
        loaderContext = context;
        videoIdNameMap = vidIdNameMap;
    }

    public void updateViews(String videoName, GalleryViewHolder holderView) {
        videoMap.put(holderView.thumbnailImage, videoName);
        Bitmap thumb = memoryCache.get(videoName);
        if (thumb != null) {
            Log.d(TAG, "Reusing image from cache");
            holderView.updateViewsOnLoaded(thumb);
            return;
        }
        queueThumbnail(videoName, holderView);
        holderView.updateViewsOnLoading();
    }

    private class GalleryItem {
        private final String videoName;
        private final GalleryViewHolder holderView;

        public GalleryItem(String filename, GalleryViewHolder view) {
            videoName = filename;
            holderView = view;
        }
    }

    private void queueThumbnail(String videoName, GalleryViewHolder holderView) {
        GalleryItem item = new GalleryItem(videoName, holderView);
        executorService.submit(new ThumbnailRunnable(item));
    }

    private class ThumbnailRunnable implements Runnable {
        GalleryItem galleryItem;
        ThumbnailRunnable(GalleryItem item) {
            galleryItem = item;
        }
        @Override
        public void run() {
            try {
                if (!isThumbViewMappedToVideo(galleryItem.videoName, galleryItem.holderView.thumbnailImage)) {
                    return;
                }
                Bitmap thumb = Thumbnails.getThumbnail(loaderContext.getContentResolver(),
                        videoIdNameMap.get(galleryItem.videoName),
                        GalleryActivity.mBucketId,
                        Thumbnails.MINI_KIND,
                        null);
                Log.d(TAG, "thumb.size = " + thumb.getByteCount());
                memoryCache.put(galleryItem.videoName, thumb);
                Log.d(TAG, "##");
                if (!isThumbViewMappedToVideo(galleryItem.videoName, galleryItem.holderView.thumbnailImage)) {
                    return;
                }
                handler.post(new UpdateViewsRunnable(galleryItem, thumb));
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private class UpdateViewsRunnable implements Runnable {
        GalleryItem galleryItem;
        Bitmap thumbnail;

        UpdateViewsRunnable(GalleryItem item, Bitmap thumb) {
            galleryItem = item;
            thumbnail = thumb;
        }

        @Override
        public void run() {
            Log.d(TAG, "UpdateViewsRunnable");
            if (!isThumbViewMappedToVideo(galleryItem.videoName, galleryItem.holderView.thumbnailImage)) {
                galleryItem.holderView.updateViewsOnLoadFailure();
                return;
            }
            if (thumbnail == null) {
                galleryItem.holderView.updateViewsOnLoadFailure();
                return;
            }
            Log.d(TAG, "UpdateViewsRunnable1");
            galleryItem.holderView.updateViewsOnLoaded(thumbnail);
        }
    }

    private boolean isThumbViewMappedToVideo(String videoName, ImageView thumbView) {
        String tag = videoMap.get(thumbView);
        if(tag == null || !tag.equals(videoName)) {
            return false;
        }
        return true;
    }

    private Bitmap getBitmap(String url)
    {
        File f = fileCache.getFile(url);

        //from SD cache
        Bitmap b = decodeFile(f);
        if(b != null)
            return b;

        //from web
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex) {
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if(width_tmp/2 < REQUIRED_SIZE || height_tmp/2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
