package jp.neoscorp.android.animov;

import jp.neoscorp.android.animov.Gallery.GalleryActivity;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GimovActivity extends Activity {
    private static final String TAG = GimovActivity.class.getSimpleName();

    OnClickListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gimov_activity);

        mListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(intent);
            }
        };

        Button openGallBtn = (Button) findViewById(R.id.btn_open_gallery);
        openGallBtn.setOnClickListener(mListener);

        String alpha = GalleryActivity.PATH + "alpha7.mp4";
        String beta = GalleryActivity.PATH + "beta7.mp4";
        MediaScannerConnection.scanFile(getApplicationContext(), new String[] { alpha, beta }, null, new OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.i(TAG, "Scan completed, path = " + path);
                Log.i(TAG, "Scan completed, uri = " + uri);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gimov, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
