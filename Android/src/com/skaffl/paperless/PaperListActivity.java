package com.skaffl.paperless;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class PaperListActivity extends FragmentActivity
        implements PaperListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_list);

        if (findViewById(R.id.paper_detail_container) != null) {
            mTwoPane = true;
            ((PaperListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.paper_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PaperDetailFragment.ARG_ITEM_ID, id);
            PaperDetailFragment fragment = new PaperDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.paper_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, PaperDetailActivity.class);
            detailIntent.putExtra(PaperDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
