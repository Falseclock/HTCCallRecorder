package call.recorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class Main extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    public static SharedPreferences preferences;
    public static final String PACKAGE_NAME = "call.recorder";
    public static final String PREFERENCE_FILE = "call.recorder_preferences";
    public static final String AUTO_REC_MAIN = "Automatic records/";
    public static final String AUTO_REC_INCOMING = AUTO_REC_MAIN + "/Incoming/";
    public static final String AUTO_REC_OUTGOING = AUTO_REC_MAIN + "/Outgoing/";


    public boolean onPreferenceChange(Preference preference, Object object) {
        return false;
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(Main.PREFERENCE_FILE,MODE_PRIVATE);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    public static boolean toBoolean(int paramInt)
    {
        return paramInt > 0;
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            addPreferencesFromResource(R.xml.settings);

            final PreferenceScreen SettingsScreen = (PreferenceScreen) findPreference("recording_screen");
            final PreferenceCategory StorageCategory = (PreferenceCategory) findPreference("storage_category");
            final PreferenceCategory FilteringCategory = (PreferenceCategory) findPreference("filtering_category");
            final PreferenceCategory AutoDeleteCategory = (PreferenceCategory) findPreference("auto_delete_category");

            findPreference("auto_recording").setDependency("call_recording");
            findPreference("storage_category").setDependency("call_recording");

            // Now let's hide all categories if recording is disabled

            if (!preferences.getBoolean("call_recording", false)) {
                SettingsScreen.removePreference(StorageCategory);
            }

            if (!preferences.getBoolean("auto_recording", false)) {
                SettingsScreen.removePreference(FilteringCategory);
                SettingsScreen.removePreference(AutoDeleteCategory);
            }

            findPreference("call_recording").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object object) {
                    Boolean value = false;
                    if (object.toString().equals("true"))
                        value = true;

                    // If we disable recording at all
                    if (!value) {
                        SettingsScreen.removePreference(StorageCategory);

                        // And remove two categories
                        SettingsScreen.removePreference(FilteringCategory);
                        SettingsScreen.removePreference(AutoDeleteCategory);
                    } else { // We enabling recording function

                        SettingsScreen.addPreference(StorageCategory);

                        if (preferences.getBoolean("auto_recording", false)) {
                            SettingsScreen.addPreference(FilteringCategory);
                            SettingsScreen.addPreference(AutoDeleteCategory);
                        }
                    }

                    return true;
                }
            });

            findPreference("auto_recording").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object object) {
                    Boolean value = false;
                    if (object.toString().equals("true"))
                        value = true;

                    if (!value) {
                        SettingsScreen.removePreference(FilteringCategory);
                        SettingsScreen.removePreference(AutoDeleteCategory);
                    } else {
                        SettingsScreen.addPreference(FilteringCategory);
                        SettingsScreen.addPreference(AutoDeleteCategory);
                    }

                    return true;
                }
            });

            /*
            int storage = Integer.parseInt(preferences.getString("storage_path", "1"));
            final String[] RecordingStorage = getResources().getStringArray(R.array.storage_category_entries);
            findPreference("storage_path").setSummary(RecordingStorage[storage]);
            findPreference("storage_path").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object object) {
                    int index = Integer.parseInt(object.toString());
                    preference.setSummary(RecordingStorage[index]);

                    return true;
                }
            });
            */
        }

    }
}