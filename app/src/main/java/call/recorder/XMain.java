package call.recorder;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XMain implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static XSharedPreferences pref;
    public static String MODULE_PATH = null;

    public void initZygote(StartupParam startupParam) throws Throwable {
        pref = new XSharedPreferences(Main.PACKAGE_NAME, Main.PREFERENCE_FILE);
        MODULE_PATH = startupParam.modulePath;
    }

    public void handleLoadPackage(LoadPackageParam paramLoadPackageParam) throws Throwable {
        String packageName = paramLoadPackageParam.packageName;

        if (packageName.equals("com.android.phone")) {
            Recorder.hookEnableCallRecording(paramLoadPackageParam);
            Recorder.hookAutomateCallRecording(paramLoadPackageParam);

            //XposedBridge.log("!!!hookEnableCallRecording");
        }

        if (packageName.equals("com.android.phone.util") || packageName.equals("com.android.phone")) {
            Recorder.hookAutomateCallRecordingFilename(paramLoadPackageParam);
        }

        if (packageName.equals("com.htc.soundrecorder")) {
			/*--------------------*/
			/* RECORDING FILENAME */
			/*--------------------*/
            Recorder.getStorageRoot(paramLoadPackageParam);
            //Recorder.hookPausableAudioRecorderStart(paramLoadPackageParam);
            // Recorder.hookIsEnableAudioRecord(paramLoadPackageParam);
        }
    }

    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable
    {
        String packageName = resparam.packageName;

    }
}
