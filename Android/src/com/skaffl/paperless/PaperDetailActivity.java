package com.skaffl.paperless;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class PaperDetailActivity extends FragmentActivity {

    private static final String TAG = "PaperDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Log.v(TAG, "savedInstanceState is null");
            Bundle arguments = new Bundle();
            arguments.putString(PaperDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(PaperDetailFragment.ARG_ITEM_ID));
            PaperDetailFragment fragment = new PaperDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.paper_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, PaperListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
