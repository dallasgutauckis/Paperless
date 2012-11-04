package com.skaffl.paperless;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skaffl.paperless.dummy.Worksheets;
import com.skaffl.paperless.view.PaperDrawingView;
import com.skaffl.paperless.view.PaperDrawingView.PersonType;

public class PaperDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    private static final String TAG = "PaperDetailFragment";

    Worksheets.Worksheet mItem;

    public PaperDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = Worksheets.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_paper_detail, container, false);

        if (mItem != null) {
            Log.v(TAG, "Loading item: " + mItem);

            PaperDrawingView worksheet = (PaperDrawingView) rootView.findViewById(R.id.worksheet);
            worksheet.setWorksheet(mItem);
            worksheet.setDrawingPersonType(PersonType.STUDENT);
        }

        return rootView;
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
}
