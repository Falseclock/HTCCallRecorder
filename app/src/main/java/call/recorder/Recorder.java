package call.recorder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XModuleResources;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.Connection;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Recorder
{
    private static Boolean is_incoming;
    private static Connection mConnection;
    private static boolean CallRecording;
    private static boolean AutoRecording;
    private static boolean RecordIn;
    private static boolean RecordOut;
    private static int RecordCaller;
    private static int AutoRecordingStorage;
    private static int SlotToRecord;

    public static void hookPausableAudioRecorderStart(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam)
    {
        // FIX ME: не работает
        try {
            findAndHookMethod("com.htc.soundrecorder.PausableAudioRecorder", paramLoadPackageParam.classLoader, "start", "long", "java.lang.String", "java.lang.String", new XC_MethodHook()
            {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable
                {
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                    int i = Integer.parseInt(preferences.getString("encodeOption", "1"));

                    param.args[1] = "audio/amr";

                    if (i == 1) {
                        param.args[1] = "audio/aac";
                    }
                    if (i == 2) {
                        param.args[1] = "audio/amr-wb";
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private static boolean isToAutoRecord(Context paramContext, ClassLoader classLoader)
    {
        CallRecording = Main.toBoolean(Settings.System.getInt(paramContext.getContentResolver(), "call_recording", 1));  //FIXME: set to 0
        AutoRecording = Main.toBoolean(Settings.System.getInt(paramContext.getContentResolver(), "auto_recording", 1));  //FIXME: set to 0
        RecordIn = Main.toBoolean(Settings.System.getInt(paramContext.getContentResolver(), "tweak_call_recording_auto_filter_in", 1));
        RecordOut = Main.toBoolean(Settings.System.getInt(paramContext.getContentResolver(), "tweak_call_recording_auto_filter_out", 1));

        RecordCaller = Settings.System.getInt(paramContext.getContentResolver(), "tweak_call_recording_auto_caller", 0);
        AutoRecordingStorage = Settings.System.getInt(paramContext.getContentResolver(), "tweak_call_recording_auto_storage", 1);
        SlotToRecord = Settings.System.getInt(paramContext.getContentResolver(), "tweak_call_recording_auto_slot", 0);

        if (!CallRecording)
            return false;

        if (!AutoRecording)
            return false;

        /* FIXME:
        // Record by slot
        if (SlotToRecord != 0) {
            if (mConnection != null) {
                int PhoneType = mConnection.getCall().getPhone().getPhoneType();
                Class<?> PhoneUtils = XposedHelpers.findClass("com.android.phone.PhoneUtils", classLoader);

                int slot = (Integer) XposedHelpers.callStaticMethod(PhoneUtils, "getSimSlotTypeByPhoneType", PhoneType);
                if (slot != SlotToRecord) {
                    return false;
                }
            }
        }

        if (is_incoming != null) {
            if (!RecordIn && is_incoming) // Если не записывать входящие и
                // звонок
                // входящий
                return false;

            if (!RecordOut && !is_incoming) // Если не записывать исходящие и
                // звонок исходящий
                return false;
        }

        String localName = null;

        if (mConnection != null) {
            Call localCall = mConnection.getCall();

            Connection localConnection = localCall.getEarliestConnection();
            if (localConnection != null) {
                Object localObject = localConnection.getUserData();
                if ((localObject instanceof CallerInfo)) {
                    CallerInfo localCallerInfo = (CallerInfo) localObject;

                    localName = localCallerInfo.name;
                }
            }
        }

        // Если только из записной книжки и имя не определено
        if (RecordCaller == 1 && localName == null)
            return false;

        // Если только неизвестные номера и имя задано
        if (RecordCaller == 2 && localName != null)
            return false;

        // Если записывать всех
        if (RecordCaller == 0)
            return true;
        */

        // Во всех остальных случая возвращаем true
        return true;
    }

    public static void hookEnableCallRecording(final XC_LoadPackage.LoadPackageParam paramLoadPackageParam) {
        findAndHookMethod("com.android.phone.PhoneApp", paramLoadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                ContentResolver cr = (ContentResolver) XposedHelpers.callMethod(param.thisObject, "getContentResolver");
                Class<?> Features = XposedHelpers.findClass("com.android.phone.HtcFeatureList", paramLoadPackageParam.classLoader);

                boolean CallRecording = Main.toBoolean(Settings.System.getInt(cr, "call_recording", 1)); //FIXME: set to 0

                if (CallRecording) {
                    XposedHelpers.setStaticBooleanField(Features, "FEATURE_SUPPORT_VOICE_RECORDING", true);
                    // XposedHelpers.setStaticBooleanField(Features,
                    // "FEATURE_DISABLE_GSM_VOICE_RECORDING", false);
                }
            }
        });
    }

    private static void updateRecordButton(Object button, boolean startRecordState, XModuleResources modRes)
    {
        /* FIXME:
        if (startRecordState) {
            XposedHelpers.callMethod(button, "setPressed", false);
            XposedHelpers.callMethod(button, "setColorOn", false);
            XposedHelpers.callMethod(button, "setText", modRes.getString(R.string.menu_start_record));
            XposedHelpers.callMethod(button, "setIconDrawable", modRes.getDrawable(R.drawable.icon_btn_recorder_on_dark));

        } else {
            XposedHelpers.callMethod(button, "setPressed", true);
            XposedHelpers.callMethod(button, "setColorOn", true);
            XposedHelpers.callMethod(button, "setText", modRes.getString(R.string.menu_stop_record));
            XposedHelpers.callMethod(button, "setIconDrawable", modRes.getDrawable(R.drawable.icon_btn_recorder_stop_on_dark));
        }
        */
    }

    public static void hookAutomateCallRecording(final LoadPackageParam paramLoadPackageParam)
    {
        // Начать автозапись
        findAndHookMethod("com.android.phone.CallNotifier", paramLoadPackageParam.classLoader, "onCallConnected", "android.os.AsyncResult", new XC_MethodHook()
        {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
                //FIXME:
                //AsyncResult asyncResult = (AsyncResult) param.args[0];
                //mConnection = (Connection) asyncResult.result;

                Object application = XposedHelpers.getObjectField(param.thisObject, "mApplication");
                Context context = (Context) XposedHelpers.callMethod(application, "getApplicationContext");

                //FIXME:
                //is_incoming = mConnection.isIncoming();

                // Если условия автозаписи нас устраивают
                if (isToAutoRecord(context, paramLoadPackageParam.classLoader)) {
                    Class<?> VoiceRecorderHelper = XposedHelpers.findClass("com.android.phone.util.VoiceRecorderHelper", paramLoadPackageParam.classLoader);
                    Object Recorder = XposedHelpers.callStaticMethod(VoiceRecorderHelper, "getInstance", context);

                    if (!(Boolean) XposedHelpers.callMethod(Recorder, "isRecording")) {
                        XposedHelpers.callMethod(Recorder, "start");
                    }
                }
            }
        });

        // заканчивать автозапись. Должен срабатывать автоматически, но иногда
        // система не закрывает звонок
        // поэтому делаем это еще раз, так как ошибок это не вызывает
        findAndHookMethod("com.android.phone.CallNotifier", paramLoadPackageParam.classLoader, "onDisconnect", "android.os.AsyncResult", new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
                Object application = XposedHelpers.getObjectField(param.thisObject, "mApplication");
                Context context = (Context) XposedHelpers.callMethod(application, "getApplicationContext");

                // Если условия автозаписи у нас сработали
                if (isToAutoRecord(context, paramLoadPackageParam.classLoader)) {
                    Context tweakContext = context.createPackageContext(Main.PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);

                    //FIXME:
                    //Intent intent = new Intent(tweakContext, TweakerService.class);
                    //intent.setAction(TweakerService.ACTION_CLEANUP_RECORDS);
                    //tweakContext.startService(intent);
                    //is_incoming = null;
                    //mConnection = null;

                    Class<?> VoiceRecorderHelper = XposedHelpers.findClass("com.android.phone.util.VoiceRecorderHelper", paramLoadPackageParam.classLoader);
                    Object Recorder = XposedHelpers.callStaticMethod(VoiceRecorderHelper, "getInstance",context);
                    // Проверяем еще раз на то, что запись идет
                    if ((Boolean) XposedHelpers.callMethod(Recorder, "isRecording")) {
                        XposedHelpers.callMethod(Recorder, "stop");
                    }
                }
            }
        });
    }

    // Переименовываем файл записи
    public static void hookAutomateCallRecordingFilename(final LoadPackageParam paramLoadPackageParam)
    {
        findAndHookMethod("com.android.phone.util.VoiceRecorderHelper", paramLoadPackageParam.classLoader, "getIncallRecordingFileName", "com.android.internal.telephony.CallManager", new XC_MethodHook()
        {
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable
            {
                // Context context = mContext;
				/*
				 * Context context = (Context)
				 * XposedHelpers.getObjectField(param.thisObject, "mContext");
				 *
				 *
				 * boolean CallRecording =
				 * Main.toBoolean(Settings.System.getInt(
				 * context.getContentResolver(), Main.TWEAK_CALL_REC, 0));
				 * boolean AutoRecording =
				 * Main.toBoolean(Settings.System.getInt(
				 * context.getContentResolver(), Main.TWEAK_CALL_REC_AUTO, 0));
				 * int AutoRecordingStorage =
				 * Settings.System.getInt(context.getContentResolver(),
				 * Main.TWEAK_CALL_REC_AUTO_STORAGE, 1);
				 */

                if (CallRecording && AutoRecording && is_incoming != null) {
                    String recordFile = (String) param.getResult();

                    // Если звонок входящий
                    if (is_incoming) {
                        if (AutoRecordingStorage == 1) {
                            recordFile = Main.AUTO_REC_INCOMING + recordFile;
                        } else {
                            recordFile = Main.AUTO_REC_MAIN + recordFile + "-IN";
                        }
                    } else {
                        if (AutoRecordingStorage == 1) {
                            recordFile = Main.AUTO_REC_OUTGOING + recordFile;
                        } else {
                            recordFile = Main.AUTO_REC_MAIN + recordFile + "-OUT";
                        }
                    }

                    param.setResult(recordFile);
                }
            }
        });
    }

    public static void getStorageRoot(final LoadPackageParam paramLoadPackageParam)
    {
        findAndHookMethod("com.htc.soundrecorder.util.FileNameGen", paramLoadPackageParam.classLoader, "getRootPathFile", new XC_MethodHook()
        {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                boolean CallRecording = Main.toBoolean(Settings.System.getInt(context.getContentResolver(), "call_recording", 1)); //FIXME: set to 0
                boolean AutoRecording = Main.toBoolean(Settings.System.getInt(context.getContentResolver(), "auto_recording", 1));  //FIXME: set to 0
                int AutoRecordingStorage = Settings.System.getInt(context.getContentResolver(), "storage_path", 1);

                if (CallRecording && AutoRecording) {
                    File RootPathFile = (File) param.getResult();
                    File recordPathIn;
                    File recordPathOut;

                    if (AutoRecordingStorage == 1) {
                        recordPathIn = new File(RootPathFile.getPath() + "/" + Main.AUTO_REC_INCOMING);
                        recordPathOut = new File(RootPathFile.getPath() + "/" + Main.AUTO_REC_OUTGOING);
                    } else {
                        recordPathIn = new File(RootPathFile.getPath() + "/" + Main.AUTO_REC_MAIN);
                        recordPathOut = new File(RootPathFile.getPath() + "/" + Main.AUTO_REC_MAIN);
                    }

                    if (!recordPathIn.exists()) {
                        if (!recordPathIn.mkdirs()) {
                            XposedBridge.log("Problem creating incoming folder");
                        }
                    }
                    if (!recordPathOut.exists()) {
                        if (!recordPathOut.mkdirs()) {
                            XposedBridge.log("Problem creating outgoing folder");
                        }
                    }

                    File nomedia = new File(RootPathFile.getPath() + "/" + Main.AUTO_REC_MAIN, ".nomedia");
                    if (!nomedia.exists()) {
                        if (!nomedia.createNewFile()) {
                            XposedBridge.log("Problem creating nomedia file");
                        }
                    }
                }
            }
        });
    }
}
