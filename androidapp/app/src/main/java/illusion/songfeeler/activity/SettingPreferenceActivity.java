package illusion.songfeeler.activity;

import illusion.songfeeler.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingPreferenceActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String DETECTOR_ADDRESS = "detector-server.address";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(
				PreferenceManager.getDefaultSharedPreferences(this), null);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		String server = sharedPreferences.getString("prefServerAddress", null);
		if (server != null) {
			server = server.trim();
			if (server.isEmpty()) server = null;
		}

		findPreference("prefServerAddress").setSummary("Current: " + server);
	}
}
