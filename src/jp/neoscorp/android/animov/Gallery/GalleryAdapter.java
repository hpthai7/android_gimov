package jp.neoscorp.android.animov.Gallery;

import java.util.ArrayList;
import java.util.Map;

import jp.neoscorp.android.animov.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter {
    private static final String TAG = GalleryAdapter.class.getSimpleName();
    private final Activity mActivity;
    private final ArrayList<String> mVideoList;
    private final ImageLoader mLoader;

    public GalleryAdapter(Activity activity, ArrayList<String> filenames) {
        this(activity, filenames, null);
    }

    public GalleryAdapter(Activity activity, ArrayList<String> filenames, Map<String, Integer> videoIdByName) {
        mActivity = activity;
        mVideoList = filenames;
        mLoader = new ImageLoader(activity.getApplicationContext(), videoIdByName);
    }

    public class GalleryViewHolder {
        public ImageView loadingView;
        public ImageView thumbnailView;
        public ImageView playView;
        public Button shareButton;

        public void initView(View view, int position) {
            loadingView = (ImageView) view.findViewById(R.id.img_loading);
            thumbnailView = (ImageView) view
                    .findViewById(R.id.img_thumbnail);
            playView = (ImageView) view.findViewById(R.id.img_play);
            shareButton = (Button) view.findViewById(R.id.btn_share);
            playView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    GalleryActivity.showToast(mActivity, "Played");
                }
            });
            shareButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    GalleryActivity.showToast(mActivity, "Shared");
                }
            });
        }

        public void updateViewsOnLoading() {
            loadingView.setVisibility(View.VISIBLE);
            thumbnailView.setVisibility(View.INVISIBLE);
            playView.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);

            loadingView.setImageResource(R.drawable.loading);
            thumbnailView.setImageBitmap(null);
            playView.setImageResource(0);
        }

        public void updateViewsOnLoadFailure() {
            loadingView.setVisibility(View.VISIBLE);
            thumbnailView.setVisibility(View.VISIBLE);
            playView.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);

            loadingView.setImageResource(0);
            thumbnailView.setImageResource(R.drawable.video_error);
            playView.setImageResource(0);
        }

        public void updateViewsOnLoaded(Bitmap thumb) {
            loadingView.setVisibility(View.INVISIBLE);
            thumbnailView.setVisibility(View.VISIBLE);
            playView.setVisibility(View.VISIBLE);
            shareButton.setVisibility(View.VISIBLE);

            loadingView.setImageResource(0);
            thumbnailView.setImageBitmap(thumb);
            playView.setImageResource(R.drawable.play);
        }

        public void updateViews(int position) {
            if (mLoader == null) {
                Log.d(TAG, "Adapter constructor is without videoIdByName map");
                return;
            }
            mLoader.updateViews(mVideoList.get(position), this);
        }
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
        GalleryViewHolder holderView;
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(
                    R.layout.gallery_item, parent, false);
            holderView = new GalleryViewHolder();
            holderView.initView(convertView, position);
            convertView.setTag(holderView);
        } else {
            holderView = (GalleryViewHolder) convertView.getTag();
        }
        holderView.updateViews(position);
        return convertView;
    }
}
