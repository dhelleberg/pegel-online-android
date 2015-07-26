package org.cirrus.mobi.pegel.md;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.PegelDataProvider;
import org.cirrus.mobi.pegel.PegelDetailHelper;
import org.cirrus.mobi.pegel.PegelGrafikView;
import org.cirrus.mobi.pegel.R;
import org.cirrus.mobi.pegel.data.MeasureEntry;
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
    private PointStore mPointStore;
    private TextView mTextViewMeasure;
    private PegelGrafikView mPegelGraphicsView;
    private TextView mTextViewTime;
    private TextView mTextViewTendency;

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
        View view = inflater.inflate(R.layout.pegel_data, container, false);

        StringBuilder headline = new StringBuilder(getArguments().getString(RIVER));
        getResources().getConfiguration();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            headline.append(' ').append(getArguments().getString(MPOINT));
        else
            headline.append('\n').append(getArguments().getString(MPOINT));

        TextView headlineView = (TextView) view.findViewById(R.id.data_headline);
        headlineView.setText(headline);

        mTextViewMeasure = (TextView) view.findViewById(R.id.data_table_measure);
        mTextViewTendency = (TextView) view.findViewById(R.id.data_table_tendency);
        mTextViewTime= (TextView) view.findViewById(R.id.data_table_time);
        mPegelGraphicsView = (PegelGrafikView) view.findViewById(R.id.PegelGrafikView);

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mPointStore = ((PegelApplication) getActivity().getApplication()).getPointStore();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPointStore.getMeasureEntry(getArguments().getString(PNR_NR))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MeasureEntry>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        //TODO: Snackbar
                    }

                    @Override
                    public void onNext(MeasureEntry measureEntry) {
                        if(isResumed()) {
                            mTextViewMeasure.setText( measureEntry.getMessung() );
                            mTextViewTendency.setText( getTendency(measureEntry.getTendenz()) );
                            mTextViewTime.setText( measureEntry.getZeit() );
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
}