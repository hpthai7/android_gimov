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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class GalleryActivity extends Activity implements LoaderCallbacks<Cursor> {
    private static final boolean DBG = false;

    private static final String TAG = GalleryActivity.class.getSimpleName();
    private static final int MEDIA_LOADER = 0;
    private static final String[] EXTENSIONS = { ".mp4", ".3gp" };
    private ListView mGalleryList;
    private GalleryAdapter mGalleryAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private Map<String, Integer> mVideoIdByName;
    private static final String BUCKET = "gallery";
    public static int sBucketId = 0;
    public static final String PATH = Environment.getExternalStorageDirectory() +
            "/animov/gallery/";

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
            loader = new CursorLoader(this,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);
            break;
        default:
            break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
        case MEDIA_LOADER:
            int idColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int bucketNameColIdx = data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int bucketIdColIdx = data.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
            int displayNameColIdx = data.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int mimeTypeColIdx = data.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
            while (data.moveToNext()) {
                if (DBG) Log.d(TAG, data.getPosition() + "_id, bucketid, bucketName, displName, mime = "
                        + data.getString(idColIdx)
                        + ", " + data.getString(bucketIdColIdx)
                        + ", " + data.getString(bucketNameColIdx)
                        + ", " + data.getString(displayNameColIdx)
                        + ", " + data.getString(mimeTypeColIdx));
                if (BUCKET.equals(data.getString(bucketNameColIdx))) {
                    if (sBucketId == 0) {
                        sBucketId = data.getInt(bucketIdColIdx);
                    }
                    if (mVideoIdByName == null) {
                        mVideoIdByName = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
                    }
                    mVideoIdByName.put(data.getString(displayNameColIdx), data.getInt(idColIdx));
                }
            }
            ArrayList<String> filenames = getVideoNames(PATH);
            if (mGalleryAdapter == null) {
                mGalleryAdapter = new GalleryAdapter(this, filenames, mVideoIdByName);
                mGalleryList = (ListView) findViewById(R.id.lv_gallery);
                mGalleryList.setAdapter(mGalleryAdapter);
            } else {
                // An underlying video having its thumbnail created
                // onLoadFinished is called once again
                mGalleryAdapter.notifyDataSetChanged();
            }
            break;
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
                if (DBG) Log.e(TAG, "Excluded file: " + name);
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
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}