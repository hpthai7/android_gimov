package jp.neoscorp.android.animov.Gallery;

import java.util.ArrayList;

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
        mActivity = activity;
        mVideoList = filenames;
        mLoader = new ImageLoader(activity.getApplicationContext());
    }

    public class GalleryViewHolder {
//        private int itemPosition;
        public ImageView loadingImage;
        public ImageView thumbnailImage;
        public ImageView playImage;
        public Button shareButton;

        public void initView(View view, int position) {
            loadingImage = (ImageView) view.findViewById(R.id.img_loading);
            thumbnailImage = (ImageView) view
                    .findViewById(R.id.img_thumbnail);
            playImage = (ImageView) view.findViewById(R.id.img_play);
            shareButton = (Button) view.findViewById(R.id.btn_share);
            playImage.setOnClickListener(new OnClickListener() {
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
            shareButton.setVisibility(View.VISIBLE);

            loadingImage.setImageResource(0);
            thumbnailImage.setImageBitmap(thumb);
            playImage.setImageResource(R.drawable.play);
        }

        public void updateViews(int position) {
            Log.d(TAG, "getView: position = " + position + ", viewHolder = " + this.hashCode());
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
