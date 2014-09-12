package jp.neoscorp.android.animov.Gallery;

import java.util.ArrayList;
import java.util.Map;

import jp.neoscorp.android.animov.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GalleryViewHolder holderView;
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
        final ImageView playImage = (ImageView) convertView.findViewById(R.id.img_play);
        playImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(mActivity,R.style.TransparentProgressDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.setContentView(R.layout.videopopup);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.dimAmount=0.9f;
                dialog.getWindow().setAttributes(lp);
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                final VideoView vv = (VideoView) dialog.findViewById(R.id.surface);
                vv.setVideoPath(GalleryActivity.PATH + mVideoList.get(position));
                vv.setZOrderOnTop(true);
                vv.start();

                vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        dialog.dismiss();
                    }
                });
            }
        });
        Button share = (Button)convertView.findViewById(R.id.btn_share);
        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ShareActivity.class);
                mActivity.startActivity(intent);
            }
        });
        return convertView;
    }

}
