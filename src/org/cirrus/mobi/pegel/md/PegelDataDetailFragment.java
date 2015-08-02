package org.cirrus.mobi.pegel.md;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.R;
import org.cirrus.mobi.pegel.data.MeasurePointDataDetails;
import org.cirrus.mobi.pegel.data.MeasureStationDetails;
import org.cirrus.mobi.pegel.data.PointStore;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dhelleberg on 26/07/15.
 */
public class PegelDataDetailFragment extends Fragment{

    private static final String PNR_NR = "PN_NR";
    private View mRooView;
    private PointStore mPointStore;
    private TableLayout mTableLayout;
    private LayoutInflater mInflater;

    public PegelDataDetailFragment() {
    }

    public static PegelDataDetailFragment getNewInstance(String pegelnumber) {
        Bundle args = new Bundle();
        PegelDataDetailFragment fragment = new PegelDataDetailFragment();
        args.putString(PNR_NR, pegelnumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRooView = inflater.inflate(R.layout.data_details, container, false);
        mTableLayout = (TableLayout) mRooView.findViewById(R.id.datatable);
        mInflater = inflater;
        return mRooView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mPointStore = ((PegelApplication) getActivity().getApplication()).getPointStore();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(false);
    }

    private void loadData(boolean forceRefresh) {
        mPointStore.getMeasurePointDetailsObserver(getActivity(), getArguments().getString(PNR_NR))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MeasurePointDataDetails[]>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        final Snackbar snackbar = Snackbar.make(mRooView, R.string.connection_error, Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
                        snackbar.show();
                    }

                    @Override
                    public void onNext(MeasurePointDataDetails[] measureStationDetailses) {
                        if(mTableLayout.getChildCount() > 1)//clean up the rows
                            mTableLayout.removeViews(1, mTableLayout.getChildCount()-1);

                        for (int i = 0; i < measureStationDetailses.length; i++) {
                            final View rowView = mInflater.inflate(R.layout.table_data_row, mTableLayout, false);
                            TextView tv = (TextView) rowView.findViewById(R.id.tableTextP);
                            tv.setText(measureStationDetailses[i].getDataType());

                            TextView tv2 = (TextView) rowView.findViewById(R.id.tableTextM);
                            String value = measureStationDetailses[i].getValue();//(Math.round(Float.parseFloat(dat[3])*100.0) / 100.0)+dat[2];
                            if(value.length() > 6)
                                value = value.substring(0,6);
                            tv2.setText(value);

                            TextView tv3 = (TextView) rowView.findViewById(R.id.tableTextD);
                            tv3.setText(measureStationDetailses[i].getTime());

                            mTableLayout.addView(rowView);

                        }

                    }
                });
    }
}
