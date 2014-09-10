package jp.neoscorp.android.animov.Gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jp.neoscorp.android.animov.R;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class GalleryActivity extends Activity implements LoaderCallbacks<Cursor> {
    private static final String TAG = GalleryActivity.class.getSimpleName();
    public static final String PATH = Environment.getExternalStorageDirectory() + "/animov/gallery/";
    private static final int MEDIA_LOADER = 0;
    private static final int THUMB_LOADER = 1;
    private static final String[] EXTENSIONS = { ".mp4", ".3gp" };
    private ListView mGalleryList;
    private GalleryAdapter mGalleryAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Gallery");

        mCallbacks = this;
        getLoaderManager().initLoader(MEDIA_LOADER, null, mCallbacks);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        switch (id) {
        case MEDIA_LOADER:
            String[] projection = new String[] {
                    MediaStore.Video.Media.BUCKET_ID,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.MIME_TYPE,
                    MediaStore.Video.Media._ID };
            Log.d(TAG, "Uri.fromFile(new File(PATH)) = " + Uri.fromFile(new File(PATH)));
            Log.d(TAG, "MediaStore.Video.Media.EXTERNAL_CONTENT_URI = " + MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            loader = new CursorLoader(this,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);
            break;
//        case THUMB_LOADER:
//            String[] thumbProjection = new String[] {
//                    MediaStore.Video.Thumbnails.DATA,
//                    MediaStore.Video.Thumbnails.VIDEO_ID,
//                    MediaStore.Video.Thumbnails.KIND,
//                    MediaStore.Video.Thumbnails._ID,
//            };
//            loader = new CursorLoader(this,
//                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
//                    thumbProjection,
//                    selection,
//                    selectionArgs,
//                    sortOrder);
//            break;
        default:
            break;
        }
        return loader;
    }

    public static int mBucketId = 0;
    public Map<String, Integer> mVideoIdNameMap;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
        case MEDIA_LOADER:
            int idColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int bucketNameColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int bucketIdColIndx = data.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
            int displayNameColIdx = data.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int mimeTypeColIdx = data.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
            Log.d(TAG, "LoaderCount = " + data.getCount());
            while (data.moveToNext()) {
                Log.d(TAG, data.getPosition() + "_id, bucketid, bucketName, displName, mime = "
                        + data.getString(idColIdx)
                        + ", " + data.getString(bucketIdColIndx)
                        + ", " + data.getString(bucketNameColIdx)
                        + ", " + data.getString(displayNameColIdx)
                        + ", " + data.getString(mimeTypeColIdx));
                if (data.getString(bucketNameColIdx).equals("gallery")) {
                    if (mBucketId == 0) {
                        mBucketId = data.getInt(bucketIdColIndx);
                    }
                    if (mVideoIdNameMap == null) {
                        mVideoIdNameMap = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
                    }
                    mVideoIdNameMap.put(data.getString(displayNameColIdx), data.getInt(idColIdx));
                }
            }
            ArrayList<String> filenames = getVideoNames(PATH);
            Log.d(TAG, "FileCOunt = " + filenames.size());
            if (mGalleryAdapter == null) {
                mGalleryAdapter = new GalleryAdapter(this, filenames, mVideoIdNameMap);
                mGalleryList = (ListView) findViewById(R.id.lv_gallery);
                mGalleryList.setAdapter(mGalleryAdapter);
            } else {
                // This case, an underlying video having its thumbnail created
                // onLoadFinished is called once again
                mGalleryAdapter.notifyDataSetChanged();
            }
            break;
//        case THUMB_LOADER:
//            int dataColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);
//            int videoIdColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.VIDEO_ID);
//            int kindColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.KIND);
//            int idColIdx2 = data.getColumnIndexOrThrow(MediaStore.Video.Thumbnails._ID);
//            while (data.moveToNext()) {
//                Log.d(TAG, "#_id, kind, videoId, data = "
//                        + data.getString(idColIdx2)
//                        + ", " + data.getString(kindColIdx)
//                        + ", " + data.getString(videoIdColIdx)
//                        + ", " + data.getString(dataColIdx));
//            }
//            break;
        default:
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<String> getVideoNames(String path) {
        ArrayList<String> itemList = null;
        File dir = new File(path);
        if (!dir.exists()) {
            return null;
        }
        for (String name : dir.list()) {
            if (name.toLowerCase(Locale.ENGLISH).endsWith(EXTENSIONS[0])
                    | name.toLowerCase(Locale.ENGLISH).endsWith(EXTENSIONS[1])) {
                if (itemList == null) {
                    itemList = new ArrayList<String>();
                }
                itemList.add(name);
            } else {
                Log.e(TAG, "Excluded file: " + name);
            }
        }
        return itemList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGalleryList.setAdapter(null);
        getLoaderManager().destroyLoader(MEDIA_LOADER);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG)
                .show();
    }
}
