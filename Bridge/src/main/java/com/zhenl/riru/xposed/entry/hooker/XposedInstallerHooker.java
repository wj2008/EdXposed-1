package com.zhenl.riru.xposed.entry.hooker;

import com.zhenl.riru.xposed.config.InstallerChooser;
import com.zhenl.riru.xposed.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static com.zhenl.riru.xposed.config.InstallerChooser.LEGACY_INSTALLER_PACKAGE_NAME;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

public class XposedInstallerHooker {

    public static void hookXposedInstaller(ClassLoader classLoader) {
        try {
            final String xposedAppClass = LEGACY_INSTALLER_PACKAGE_NAME + ".XposedApp";
            final Class InstallZipUtil = findClass(LEGACY_INSTALLER_PACKAGE_NAME
                    + ".util.InstallZipUtil", classLoader);
            findAndHookMethod(xposedAppClass, classLoader, "getActiveXposedVersion",
                    XC_MethodReplacement.returnConstant(XposedBridge.getXposedVersion()));
            findAndHookMethod(xposedAppClass, classLoader,
                    "reloadXposedProp", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Utils.logD("before reloadXposedProp...");
                            final String propFieldName = "mXposedProp";
                            final Object thisObject = param.thisObject;
                            if (getObjectField(thisObject, propFieldName) != null) {
                                param.setResult(null);
                                Utils.logD("reloadXposedProp already done, skip...");
                                return;
                            }
                            File file = new File("/system/framework/edconfig.dex");
                            FileInputStream is = null;
                            try {
                                is = new FileInputStream(file);
                                Object props = callStaticMethod(InstallZipUtil,
                                        "parseXposedProp", is);
                                synchronized (thisObject) {
                                    setObjectField(thisObject, propFieldName, props);
                                }
                                Utils.logD("reloadXposedProp done...");
                                param.setResult(null);
                            } catch (IOException e) {
                                Utils.logE("Could not read " + file.getPath(), e);
                            } finally {
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException ignored) {
                                    }
                                }
                            }
                        }
                    });
            findAndHookMethod(LEGACY_INSTALLER_PACKAGE_NAME + ".LogsFragment", classLoader,
                    "reloadErrorLog", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedHelpers.setObjectField(param.thisObject, "mFileErrorLog",
                                    new File("/data/local/tmp/" + InstallerChooser.INSTALLER_PACKAGE_NAME + "/log/error.log"));
                        }
                    });
        } catch (Throwable t) {
            Utils.logE("Could not hook Xposed Installer", t);
        }
    }
}
