package com.warfactory.curvesandpolygons.activities;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.warfactory.curvesandpolygons.R;
import com.warfactory.curvesandpolygons.services.OldTimesScrWallpaperService;

public class WallpaperPreferencesActivity extends PreferenceActivity 
implements OnPreferenceClickListener, OnPreferenceChangeListener{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  

		// this preference is for the green pudding wall paper
		getPreferenceManager().setSharedPreferencesName(OldTimesScrWallpaperService.SHARED_PREFS_NAME);
		 
		// populate the pref screen with xml content
		addPreferencesFromResource(R.xml.preferences);

//		// add numeric input constraints on the radius pref
//		EditTextPreference radiusPref = (EditTextPreference)findPreference(getString(R.string.pref_pudding_radius_key));
//		radiusPref.getEditText().setKeyListener(DigitsKeyListener.getInstance());
//		// validate the input on user change preference
//		radiusPref.setOnPreferenceChangeListener(this);
//		
//		// add numeric input constraints on the num of nodes pref
//		EditTextPreference numOfNodesPref = (EditTextPreference)findPreference(getString(R.string.pref_number_of_nodes_key));
//		numOfNodesPref.getEditText().setKeyListener(DigitsKeyListener.getInstance());
//		// validate the input on user change preference
//		numOfNodesPref.setOnPreferenceChangeListener(this);
//		
//		// add a listener to the pudding color & BG color pref to save the user selected color value
//		PreferenceScreen puddingColorPref = (PreferenceScreen)findPreference(getString(R.string.pref_pudding_color_key));
//		PreferenceScreen backgroundColorPref = (PreferenceScreen)findPreference(getString(R.string.pref_background_color_key));
//		puddingColorPref.setOnPreferenceClickListener(this);
//		backgroundColorPref.setOnPreferenceClickListener(this);
	}


	@Override
	public boolean onPreferenceChange(Preference pref, Object value) {

		return false;
	}


	@Override
	public boolean onPreferenceClick(Preference arg0) {
		return false;
	}
}