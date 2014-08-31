package com.alvi.clickongame;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private ClickOnView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout layout=(RelativeLayout)findViewById(R.id.relativeLayout);
        view=new ClickOnView(this, getPreferences(Context.MODE_PRIVATE), layout);
        layout.addView(view,0);
       
    }
  @Override
protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();
	view.Puase();
	
}
@Override
protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
	view.resume(this);
}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
