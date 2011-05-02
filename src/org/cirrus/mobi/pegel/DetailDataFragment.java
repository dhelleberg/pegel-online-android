package org.cirrus.mobi.pegel;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Detailed View of the data
 */
public class DetailDataFragment extends Fragment {

	private AbstractPegelDetail abstractPegelDetail;
	private String mpoint;
	private String river;
	private String pnr;

	public DetailDataFragment(String pnr, String river, String mpoint) {
		this.pnr = pnr;
		this.river = river;
		this.mpoint = mpoint;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		abstractPegelDetail.showData(pnr, river, mpoint);		
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);

		View dataView  = inflater.inflate(R.layout.data, container, false);

		PegelGrafikView pgv = (PegelGrafikView) dataView.findViewById(R.id.PegelGrafikView);

		this.abstractPegelDetail = new AbstractPegelDetail(getActivity(), pgv);

		return dataView;
	}


}
