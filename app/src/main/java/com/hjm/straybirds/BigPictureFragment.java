package com.hjm.straybirds;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hjm.straybirds.tools.LruCacheUtils;

/**
 * Created by hejunming on 2018/4/11.
 */

public class BigPictureFragment extends Fragment {

    private ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_big_picture_viewpage_item, container, false);
        mImageView = view.findViewById(R.id.big_picture_vp_item_iv);
        String path = getArguments().getString("IMAGE_PATH");
        Bitmap bm = LruCacheUtils.getInstance().loadBitmap(getActivity(), path, LruCacheUtils.BIG_PICTURE);
        if (bm != null && !bm.isRecycled()) {
            mImageView.setImageBitmap(bm);
        } else {
            mImageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.mood_sad));
        }

        return view;
    }
}
