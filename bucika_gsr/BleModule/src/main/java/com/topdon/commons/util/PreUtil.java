package com.topdon.commons.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class PreUtil {
    private WeakReference<Context> mContext;
    private SharedPreferences preferences;
    private final String DATA_URL = "/data/data/";
    private final String SHARED_PREFS = "/shared_prefs";
    private static String SHARE_NAME = "ad900_data";

    private static PreUtil instance;

    /**
     * 构造方法
     *
     * @param context
     */
    private PreUtil(Context context) {
        this(context, SHARE_NAME);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param shareName
     */
    private PreUtil(Context context, String shareName) {
        mContext = new WeakReference<>(context);
        preferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
    }

    /**
     * 得到单例模式的PreferencesManager对象
     *
     * @param context 上下文
     * @return
     */
    public static PreUtil getInstance(Context context) {
        return getInstance(context, SHARE_NAME);
    }

    /**
     * 得到单例模式的PreferencesManager对象
     *
     * @param context   上下文
     * @param shareName 文件名称
     * @return
     */
    public static PreUtil getInstance(Context context,
                                      String shareName) {
        if (instance == null) {
            synchronized (PreUtil.class) {
                if (instance == null) {
                    instance = new PreUtil(context, shareName);
                }
            }
        }
        return instance;
    }

    public void put(String key, boolean value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            if (!TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }
            edit.putBoolean(key, value);
            edit.commit();
        }
    }

    public void put(String key, String value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            if (!TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }
            edit.putString(key, value);
            edit.commit();
        }
    }

    public void put(String key, int value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            if (!TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }
            edit.putInt(key, value);
            edit.commit();
        }
    }

    public void put(String key, float value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            if (!TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }
            edit.putFloat(key, value);
            edit.commit();
        }
    }

    public void put(String key, long value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            if (!TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }
            edit.putLong(key, value);
            edit.commit();
        }
    }

    public void put(String key, Set<String> value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            if (!TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }
            edit.putStringSet(key, value);
            edit.commit();
        }
    }

    /**
     * 直接存放对象，反射将根据对象的属性作为key，并将对应的值保存。
     *
     * @param t
     */
    @SuppressWarnings("rawtypes")
    public <T> void put(T t) {
        try {
            String methodName = "";
            String savekey = "";
            String saveValue = "";
            Editor edit = preferences.edit();
            Class cls = t.getClass();

            if (edit != null) {
                Method[] methods = cls.getDeclaredMethods();
                for (Method method : methods) {
                    methodName = method.getName();
                    if (methodName != null && methodName.startsWith("get")) {
                        Object value = method.invoke(t);
                        if (!TextUtils.isEmpty(String.valueOf(value))) {
                            saveValue = String.valueOf(value);
                        }
                        savekey = methodName.replace("get", "");
                        savekey = savekey.toLowerCase();
                        edit.putString(savekey, saveValue);
                    }
                }
                edit.commit();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getString(key, "");
    }

    public String get(String key, String defValue) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getString(key, defValue);
    }

    public boolean get(String key, boolean defValue) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getBoolean(key, defValue);
    }

    public int get(String key, int defValue) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getInt(key, defValue);
    }

    public float get(String key, float defValue) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getFloat(key, defValue);
    }

    public long get(String key, long defValue) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getLong(key, defValue);
    }

    @SuppressLint("NewApi")
    public Set<String> get(String key, Set<String> defValue) {
        if (!TextUtils.isEmpty(key)) {
            key = key.toLowerCase();
        }
        return preferences.getStringSet(key, defValue);
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, Object defaultObj) {
        if (defaultObj instanceof String) {
            preferences.edit().putString(key, (String) defaultObj);
        } else if (defaultObj instanceof Integer) {
            preferences.edit().putInt(key, (Integer) defaultObj);
        } else if (defaultObj instanceof Boolean) {
            preferences.edit().putBoolean(key, (Boolean) defaultObj);
        } else if (defaultObj instanceof Float) {
            preferences.edit().putFloat(key, (Float) defaultObj);
        } else if (defaultObj instanceof Long) {
            preferences.edit().putLong(key, (Long) defaultObj);
        }
        preferences.edit().commit();
    }

    public Object get(String key, Object defaultObj) {
        if (defaultObj instanceof String) {
            return preferences.getString(key, (String) defaultObj);
        } else if (defaultObj instanceof Integer) {
            return preferences.getInt(key, (Integer) defaultObj);
        } else if (defaultObj instanceof Boolean) {
            return preferences.getBoolean(key, (Boolean) defaultObj);
        } else if (defaultObj instanceof Float) {
            return preferences.getFloat(key, (Float) defaultObj);
        } else if (defaultObj instanceof Long) {
            return preferences.getLong(key, (Long) defaultObj);
        }
        return null;
    }

    /**
     * 获取整个对象，跟put(T t)对应使用， 利用反射得到对象的属性，然后从preferences获取
     *
     * @param cls
     * @return
     */
    public <T> Object get(Class<T> cls) {
        Object obj = null;
        String fieldName = "";
        try {
            obj = cls.newInstance();
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                fieldName = f.getName();
                if (!"serialVersionUID".equals(fieldName)) {
                    f.setAccessible(true);
                    f.set(obj, get(f.getName()));
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void clearAll() {
        try {
            String fileName = SHARE_NAME + ".xml";
            StringBuilder path = new StringBuilder(DATA_URL).append(mContext.get().getPackageName()).append(SHARED_PREFS);
            File file = new File(path.toString(), fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
