package jp.neoscorp.android.animov.Gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import jp.neoscorp.android.animov.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class GalleryActivity extends Activity {
    private static final String TAG = GalleryActivity.class.getSimpleName();
    public static final String PATH = Environment.getExternalStorageDirectory() + "/animov/gallery/";
    private static final String[] EXTENSIONS = { ".mp4", ".3gp" };
    private ListView mGalleryList;
    private GalleryAdapter mGalleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Gallery");

        ArrayList<String> filenames = getVideoNames(PATH);
        mGalleryAdapter = new GalleryAdapter(this, filenames);
        mGalleryList = (ListView) findViewById(R.id.lv_gallery);
        mGalleryList.setAdapter(mGalleryAdapter);
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
        for (File child : dir.listFiles()) {
            if (child.getName().toLowerCase(Locale.ENGLISH).endsWith(EXTENSIONS[0])
                    | child.getName().toLowerCase(Locale.ENGLISH).endsWith(EXTENSIONS[1])) {
                if (itemList == null) {
                    itemList = new ArrayList<String>();
                }
                itemList.add(child.getName());
            }
        }
        return itemList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGalleryList.setAdapter(null);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG)
                .show();
    }
}
