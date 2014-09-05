package jp.neoscorp.android.animov.Gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import jp.neoscorp.android.animov.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class GalleryActivity extends Activity {
    private static final String TAG = GalleryActivity.class.getSimpleName();
    private static final String PATH = Environment
            .getExternalStorageDirectory() + "/animov/gallery/";
    private static final String[] EXTENSIONS = { ".mp4", ".3gp" };

    private ListView mGalleryList;
    private GalleryAdapter mGalleryAdapter;

    public class VideoItem {
        private String filename;
        private Bitmap thumbnail;

        public VideoItem(String name) {
            this(name, null);
        }

        public VideoItem(String name, Bitmap thumb) {
            filename = name;
            thumbnail = thumb;
        }

        public void setName(String name) {
            filename = name;
        }

        public void setThumbnail(Bitmap thumb) {
            thumbnail = thumb;
        }

        public String getName() {
            return filename;
        }

        public Bitmap getThumbnail() {
            return thumbnail;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Gallery");

        ArrayList<VideoItem> videoList = getVideoItems(PATH);
        mGalleryAdapter = new GalleryAdapter();
        mGalleryAdapter.setVideoList(videoList);
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

    public ArrayList<VideoItem> getVideoItems(String path) {
        ArrayList<VideoItem> itemList = null;
        File dir = new File(path);
        if (!dir.exists()) {
            return null;
        }
        VideoItem item = null;
        for (File child : dir.listFiles()) {
            if (child.getName().toLowerCase(Locale.ENGLISH).endsWith(EXTENSIONS[0])
                    | child.getName().toLowerCase(Locale.ENGLISH).endsWith(EXTENSIONS[1])) {
                item = new VideoItem(child.getName());
                if (itemList == null) {
                    itemList = new ArrayList<GalleryActivity.VideoItem>();
                }
                itemList.add(item);
            }
        }
        return itemList;
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    public class GalleryAdapter extends BaseAdapter {
        private ArrayList<VideoItem> mVideoList;

        public class GalleryViewHolder {
            private int itemPosition;
            private ImageView loadingImage;
            private ImageView thumbnailImage;
            private ImageView playImage;
            private Button shareButton;

            public void initView(View view, int position) {
                loadingImage = (ImageView) view.findViewById(R.id.img_loading);
                thumbnailImage = (ImageView) view
                        .findViewById(R.id.img_thumbnail);
                playImage = (ImageView) view.findViewById(R.id.img_play);
                shareButton = (Button) view.findViewById(R.id.btn_share);
                playImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showToast("Played");
                    }
                });
                shareButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showToast("Shared");
                    }
                });
            }

            public void updateViewsOnLoading() {
                loadingImage.setVisibility(View.VISIBLE);
                thumbnailImage.setVisibility(View.INVISIBLE);
                playImage.setVisibility(View.INVISIBLE);
                shareButton.setVisibility(View.INVISIBLE);

                loadingImage.setImageResource(R.drawable.loading);
                thumbnailImage.setImageBitmap(null);
                playImage.setImageResource(0);
            }

            public void updateViewsOnLoadFailure() {
                loadingImage.setVisibility(View.VISIBLE);
                thumbnailImage.setVisibility(View.VISIBLE);
                playImage.setVisibility(View.INVISIBLE);
                shareButton.setVisibility(View.INVISIBLE);

                loadingImage.setImageResource(0);
                thumbnailImage.setImageResource(R.drawable.video_error);
                playImage.setImageResource(0);
            }

            public void updateViewsOnLoaded(Bitmap thumb) {
                loadingImage.setVisibility(View.INVISIBLE);
                thumbnailImage.setVisibility(View.VISIBLE);
                playImage.setVisibility(View.VISIBLE);

                loadingImage.setImageResource(0);
                thumbnailImage.setImageBitmap(thumb);
                playImage.setImageResource(R.drawable.play);
            }

            public void fillItem(int position) {
                itemPosition = position;
                updateViewsOnLoading();;
                VideoItem videoItem = mVideoList.get(position);
                if (videoItem.getThumbnail() != null) {
                    thumbnailImage.setImageBitmap(videoItem.getThumbnail());
                    updateViewsOnLoaded(videoItem.getThumbnail());
                    return;
                }
                new ThumbnailTask(this, position).
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoItem.getName());

            }
        }

        public void setVideoList(ArrayList<VideoItem> list) {
            mVideoList = list;
        }

        @Override
        public int getCount() {
            return (mVideoList == null) ? 0 : mVideoList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GalleryViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.gallery_item, parent, false);
                viewHolder = new GalleryViewHolder();
                viewHolder.initView(convertView, position);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GalleryViewHolder) convertView.getTag();
            }
            Log.d(TAG, "getView: position = " + position + ", viewHolder = " + viewHolder.hashCode());
            viewHolder.fillItem(position);
            return convertView;
        }

        public class ThumbnailTask extends AsyncTask<String, Void, Bitmap> {
            private final int mPosition;
            private final GalleryViewHolder mViewHolder;

            public ThumbnailTask(GalleryViewHolder holder, int position) {
                mPosition = position;
                mViewHolder = holder;
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                return ThumbnailUtils.createVideoThumbnail(PATH + params[0],
                        Thumbnails.FULL_SCREEN_KIND);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                Log.d(TAG, "onPostExecute mPosition = " + mPosition + ", mViewHolder.itemPosition = " + mViewHolder.itemPosition + ", mViewHolder = " + mViewHolder.hashCode());
                if (mViewHolder.itemPosition == mPosition) {
                    if (result == null) {
                        mViewHolder.updateViewsOnLoadFailure();
                        return;
                    }
                    mViewHolder.updateViewsOnLoaded(result);
                    mVideoList.get(mPosition).setThumbnail(result);
                }
            }
        }

    }
}
