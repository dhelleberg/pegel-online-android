package org.cirrus.mobi.pegel.md;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.R;
import org.cirrus.mobi.pegel.data.PointStore;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dhelleberg on 02/08/15.
 */
public class PegelMapFragment extends Fragment {

    private static final String PNR_NR = "PNR_NR";
    private View mRooView;
    private ImageView mMap;
    private PointStore mPointStore;

    public PegelMapFragment() {
    }

    public static PegelMapFragment newInstance(String pnr) {

        Bundle args = new Bundle();
        PegelMapFragment fragment = new PegelMapFragment();
        args.putString(PNR_NR, pnr);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRooView = inflater.inflate(R.layout.map_view, container, false);
        mMap = (ImageView) mRooView.findViewById(R.id.map_image);
        return mRooView;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mPointStore = ((PegelApplication) getActivity().getApplication()).getPointStore();
    }

    @Override
    public void onStart() {
        super.onStart();
        final ImageView map = (ImageView) mRooView.findViewById(R.id.map_image);
        final ViewTreeObserver vto = map.getViewTreeObserver();
        // we need to let the layout finish first in order to get the correct size of the image, then request the map
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int finalHeight = map.getMeasuredHeight();
                int finalWidth = map.getMeasuredWidth();
                int size = Math.min(finalHeight, finalWidth);

                mPointStore.getMapURL(getArguments().getString(PNR_NR), size)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {
                                final Snackbar snackbar = Snackbar.make(mRooView, R.string.connection_error, Snackbar.LENGTH_LONG);
                                snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
                                snackbar.show();
                            }

                            @Override
                            public void onNext(String s) {
                                Glide.with(PegelMapFragment.this).load(s)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(mMap);
                            }
                        });
                final ViewTreeObserver vto = map.getViewTreeObserver();
                vto.removeOnPreDrawListener(this);
                return true;
            }
        });

    }

    private int getSize() {

        int size = Math.min(mMap.getWidth(), mMap.getHeight());
        return size;
    }
}
