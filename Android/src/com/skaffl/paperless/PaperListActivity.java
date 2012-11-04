package com.skaffl.paperless;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.skaffl.paperless.dummy.Worksheets;

public class PaperListActivity extends FragmentActivity implements PaperListFragment.Callbacks, Constants {
    protected static final String TAG = "PaperListActivity";

    private boolean mTwoPane;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tools, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            Worksheets.getFiles();
            return true;
        } else if (item.getItemId() == R.id.save) {
            Worksheets.saveFiles(this);
            return true;
        } else if (item.getItemId() == R.id.reset) {
            Worksheets.resetStudent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

        Worksheets.init(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
