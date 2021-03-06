package org.cirrus.mobi.pegel.md;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.leakcanary.RefWatcher;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.PegelGrafikView;
import org.cirrus.mobi.pegel.R;
import org.cirrus.mobi.pegel.data.MeasureEntry;
import org.cirrus.mobi.pegel.data.MeasureStationDetails;
import org.cirrus.mobi.pegel.data.PointStore;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dhelleberg on 25/07/15.
 */
public class PegelDataFragment extends Fragment {

    private static final String PNR_NR = "PNR";
    private static final String RIVER = "RIVER";
    private static final String MPOINT = "MPOINT";
    private static final String TAG = "PegelDataFragment";
    private PointStore mPointStore;

    private TextView mTextViewMeasure;
    private PegelGrafikView mPegelGraphicsView;
    private TextView mTextViewTime;
    private TextView mTextViewTendency;
    private View mRooView;
    private ImageView mPegelDataImageView;
    private RefreshIndicatorInterface mRefreshIndicator;

    public PegelDataFragment() {
    }

    public static PegelDataFragment getNewInstance(String pegelnumber, String river, String mpoint) {
        PegelDataFragment pegelDataFragment = new PegelDataFragment();
        Bundle args = new Bundle();
        args.putString(PNR_NR, pegelnumber);
        args.putString(RIVER, river);
        args.putString(MPOINT, mpoint);
        pegelDataFragment.setArguments(args);
        return pegelDataFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRooView = inflater.inflate(R.layout.pegel_data, container, false);

        StringBuilder headline = new StringBuilder(getArguments().getString(RIVER));
        getResources().getConfiguration();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            headline.append(' ').append(getArguments().getString(MPOINT));
        else
            headline.append('\n').append(getArguments().getString(MPOINT));

        TextView headlineView = (TextView) mRooView.findViewById(R.id.data_headline);
        headlineView.setText(headline);

        mTextViewMeasure = (TextView) mRooView.findViewById(R.id.data_table_measure);
        mTextViewTendency = (TextView) mRooView.findViewById(R.id.data_table_tendency);
        mTextViewTime= (TextView) mRooView.findViewById(R.id.data_table_time);
        mPegelGraphicsView = (PegelGrafikView) mRooView.findViewById(R.id.PegelGrafikView);
        mPegelDataImageView = (ImageView) mRooView.findViewById(R.id.data_image);

        return mRooView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mPointStore = ((PegelApplication) getActivity().getApplication()).getPointStore();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mRefreshIndicator = (RefreshIndicatorInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RefreshIndicatorInterface");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadData(false);
    }

    public void loadData(final boolean refresh) {
        mRefreshIndicator.isRefreshing(true);
        mPointStore.getMeasureEntry(getArguments().getString(PNR_NR))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MeasureEntry>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        final Snackbar snackbar = Snackbar.make(mRooView, R.string.connection_error, Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
                        snackbar.show();
                        mRefreshIndicator.isRefreshing(false);
                    }

                    @Override
                    public void onNext(MeasureEntry measureEntry) {
                        if (isResumed()) {
                            mTextViewMeasure.setText(measureEntry.getMessung());
                            mTextViewTendency.setText(getTendency(measureEntry.getTendenz()));
                            mTextViewTime.setText(measureEntry.getZeit());
                            mPegelGraphicsView.setMeasure(Float.parseFloat(measureEntry.getMessung()));
                            mRefreshIndicator.isRefreshing(false);
                        }
                    }
                });

        mPointStore.getMeasureLineImageURL(getArguments().getString(PNR_NR))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String s) {
                        if (isResumed()) {
                            Log.d(TAG, "Loading image: " + s);
                            if (refresh)
                                Glide.with(PegelDataFragment.this).load(s)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(mPegelDataImageView);
                            else
                                Glide.with(PegelDataFragment.this).load(s)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(mPegelDataImageView);
                        }
                    }
                });

        mPointStore.getMeasureStationDetails(getActivity(), getArguments().getString(PNR_NR))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MeasureStationDetails[]>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        final Snackbar snackbar = Snackbar.make(mRooView, R.string.connection_error, Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
                        snackbar.show();
                        mRefreshIndicator.isRefreshing(false);
                    }

                    @Override
                    public void onNext(MeasureStationDetails[] measureStationDetailses) {
                        for (int i = 0; i < measureStationDetailses.length; i++) {
                            if(measureStationDetailses[i].getName().equals("HSW")) {
                                mPegelGraphicsView.setHSW(Float.parseFloat(measureStationDetailses[i].getValue()));
                                mPegelGraphicsView.setVisibility(View.VISIBLE);
                            }
                            else if(measureStationDetailses[i].getName().equals("M_I"))
                                mPegelGraphicsView.addAdditionalData("M_I", measureStationDetailses[i].getValue());
                        }



                    }
                });
    }


    private String getTendency(String tendencyNr) {
        String tendency = "";
        int t = Integer.parseInt(tendencyNr);
        switch (t) {
            case 0:
                tendency = getResources().getText(R.string.tendency_constant).toString();
                break;
            case 1:
                tendency = getResources().getText(R.string.tendency_up).toString();
                break;
            case -1:
                tendency = getResources().getText(R.string.tendency_down).toString();
                break;

            default:
                tendency = getResources().getText(R.string.tendency_unknown).toString();
                break;
        }
        return tendency;
    }
    @Override public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = PegelApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}