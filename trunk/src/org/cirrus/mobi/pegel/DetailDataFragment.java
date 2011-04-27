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
		
		@Override
		public View onCreateView(LayoutInflater inflater,
				ViewGroup container, Bundle savedInstanceState) {

			View dataView  = inflater.inflate(R.layout.data, container, false);

			PegelGrafikView pgv = (PegelGrafikView) dataView.findViewById(R.id.PegelGrafikView);
			
			this.abstractPegelDetail = new AbstractPegelDetail(getActivity(), pgv);
			
			return dataView;
		}

		public void showData(String pnr, String river, String mpoint) {
			abstractPegelDetail.showData(pnr, river, mpoint);			
		}

	}
