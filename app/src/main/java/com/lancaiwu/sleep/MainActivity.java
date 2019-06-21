package com.lancaiwu.sleep;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lancaiwu.sleep.bean.AppBean;
import com.lancaiwu.sleep.bean.SettingBean;
import com.lancaiwu.sleep.bean.TimeBean;
import com.lancaiwu.sleep.utils.AppUtils;
import com.lancaiwu.sleep.utils.Constants;
import com.lancaiwu.sleep.utils.IOUtils;
import com.lancaiwu.sleep.utils.SpUtils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.lancaiwu.sleep.utils.Constants.SP_NAME;

public class MainActivity extends AppCompatActivity {

    private SettingBean settingBean;

    private Gson gson = new Gson();

    private TextView tv_xp_state;

    private TextView tv_start_time;

    private TextView tv_end_time;

    private Button btn_set_start_time;

    private Button btn_set_end_time;

    private Switch sb_setting;

    private TextView tv_setting_state;

    private TextView tv_app_name;

    private Button btn_set_white_list;

    private List<AppBean> appList;

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showLauncherIcon(false);

        initPer();
    }


    public void showLauncherIcon(boolean isShow) {
        PackageManager packageManager = this.getPackageManager();
        int show = isShow ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(getAliseComponentName(), show, PackageManager.DONT_KILL_APP);
    }

    private ComponentName getAliseComponentName() {
        return new ComponentName(MainActivity.this, "com.lancaiwu.sleep.MainActivity-Alias");
        // 在AndroidManifest.xml中为MainActivity定义了一个别名为MainActivity-Alias的activity，是默认启动activity、是点击桌面图标后默认程序入口
    }

    private void initPer() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            } else {
                init();
            }
        }

    }


    private void init() {

        initData();

        initView();

        initAppList();

        saveSetting();


    }

    private void initAppList() {
        appList = AppUtils.getThirdAppList(this);
    }

    /**
     * 初始化 数据
     */
    private void initData() {
        // 文件里面没有东西，读取sp里面的数据
        SharedPreferences sharedPreferences = getSp();
        settingBean = SpUtils.getSetting(sharedPreferences);
        //    initDataBean();
        saveSetting();
    }

    /**
     * 初始化UI
     */
    private void initView() {
        // Example of a call to a native method
        tv_xp_state = (TextView) findViewById(R.id.tv_xp_state);
        tv_start_time = (TextView) findViewById(R.id.tv_start_time);
        tv_end_time = (TextView) findViewById(R.id.tv_end_time);
        btn_set_start_time = (Button) findViewById(R.id.btn_set_start_time);
        btn_set_end_time = (Button) findViewById(R.id.btn_set_end_time);
        sb_setting = (Switch) findViewById(R.id.sb_setting);
        tv_setting_state = (TextView) findViewById(R.id.tv_setting_state);

        btn_set_white_list = (Button) findViewById(R.id.btn_set_white_list);
        tv_app_name = (TextView) findViewById(R.id.tv_app_name);

        if (isEnableXP()) {
            tv_xp_state.setText("已激活");
            tv_xp_state.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            tv_xp_state.setText("未激活");
            tv_xp_state.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        tv_start_time.setText(getTimeFormat(settingBean.getStartTime()));
        btn_set_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimeDialog(new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        TimeBean timeBean = new TimeBean();
                        timeBean.setHour(hourOfDay);
                        timeBean.setMinute(minute);
                        settingBean.setStartTime(timeBean);
                        tv_start_time.setText(getTimeFormat(settingBean.getStartTime()));
                        saveSetting();
                    }
                }, settingBean.getStartTime());
            }
        });

        tv_end_time.setText(getTimeFormat(settingBean.getEndTime()));
        btn_set_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimeDialog(new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //同DatePickerDialog控件
                        TimeBean timeBean = new TimeBean();
                        timeBean.setHour(hourOfDay);
                        timeBean.setMinute(minute);
                        settingBean.setEndTime(timeBean);
                        tv_end_time.setText(getTimeFormat(settingBean.getEndTime()));
                        saveSetting();
                    }
                }, settingBean.getEndTime());
            }
        });

        if (settingBean.isEnable()) {
            tv_setting_state.setText("(已开启)");
            tv_setting_state.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            tv_setting_state.setText("(已关闭)");
            tv_setting_state.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        sb_setting.setChecked(settingBean.isEnable());
        sb_setting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingBean.setEnable(isChecked);
                if (isChecked) {
                    tv_setting_state.setText("(已开启)");
                    tv_setting_state.setTextColor(getResources().getColor(R.color.colorGreen));
                } else {
                    tv_setting_state.setText("(已关闭)");
                    tv_setting_state.setTextColor(getResources().getColor(R.color.colorAccent));
                }
                saveSetting();
            }
        });

        btn_set_white_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetWhiteList();
            }
        });

        if (settingBean.getAppBeans() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (AppBean appBean : settingBean.getAppBeans()) {
                stringBuilder.append(appBean.getAppName()).append(" ");
            }
            tv_app_name.setText(stringBuilder.toString());
        } else {
            tv_app_name.setText("暂未设置");
        }

    }

    private void showSetWhiteList() {

        Comparator<AppBean> comparator = new Comparator<AppBean>() {
            public int compare(AppBean s1, AppBean s2) {
                Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                return com.compare(s1.getAppName(), s2.getAppName());
            }
        };
        Collections.sort(appList, comparator);

        final String[] appNames = new String[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            appNames[i] = appList.get(i).getAppName();
        }


        //   Comparator<Object> comparator = Collator.getInstance(java.util.Locale.CHINA);


        //   Arrays.sort(appNames, comparator);

//        AlertDialog.Builder singleChoiceDialog =
//                new AlertDialog.Builder(MainActivity.this);
//        singleChoiceDialog.setTitle("请选择一个忽略的app");
//        // 第二个参数是默认选项，此处设置为0
//        final int[] myWhich = {-1};
//        singleChoiceDialog.setSingleChoiceItems(appNames, 0,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        myWhich[0] = which;
//                    }
//                });
//        singleChoiceDialog.setPositiveButton("确定",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.e("lanc", "which: " + myWhich[0]);
//                        if (myWhich[0] >= 0) {
//                            AppBean appBean = appList.get(myWhich[0]);
//                            settingBean.setAppBean(appBean);
//                            tv_app_name.setText(settingBean.getAppBean().getAppName());
//                            saveSetting();
//                        }
//                    }
//                });
//        singleChoiceDialog.show();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final boolean[] checkedItems = new boolean[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            checkedItems[i] = false;
        }

        builder.setMultiChoiceItems(appNames, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {

                    }
                });


        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // 把选中的 条目的数据给我取出来
                List<AppBean> appBeanList = new ArrayList<>();

                StringBuilder stringBuffer = new StringBuilder();

                for (int i = 0; i < checkedItems.length; i++) {
                    // 判断一下 选中的
                    if (checkedItems[i]) {
                        appBeanList.add(appList.get(i));
                        stringBuffer.append(appList.get(i).getAppName()).append(" ");
                    }
                }

                settingBean.setAppBeans(appBeanList);
                tv_app_name.setText(stringBuffer.toString());
                saveSetting();
                // 关闭对话框
                dialog.dismiss();

            }
        });
        // 最后一步 一定要记得 和Toast 一样 show出来
        builder.show();
    }

    private String getTimeFormat(TimeBean timeBean) {
        return (String.format("%02d", timeBean.getHour()) + ":" + String.format("%02d", timeBean.getMinute()));
    }

    public boolean isEnableXP() {
        return false;
    }

    private void setTimeDialog(TimePickerDialog.OnTimeSetListener timeSetListener, TimeBean timeBean) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, timeSetListener, timeBean.getHour(), timeBean.getMinute(), true);
        timePickerDialog.show();
    }

    /**
     * 将配置写入文件
     */
    private void saveSetting() {
        try {
            SharedPreferences sharedPreferences = getSp();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isEnable", settingBean.isEnable());
            editor.putInt("startTime_hour", settingBean.getStartTime().getHour());
            editor.putInt("startTime_minute", settingBean.getStartTime().getMinute());
            editor.putInt("endTime_hour", settingBean.getEndTime().getHour());
            editor.putInt("endTime_minute", settingBean.getEndTime().getMinute());

            if (settingBean.getAppBean() != null) {
                editor.putString("white_list_app", gson.toJson(settingBean.getAppBean()));
            }
            if (settingBean.getAppBeans() != null) {
                editor.putString("white_list_apps", gson.toJson(settingBean.getAppBeans()));
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            // 检测 目录
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.FILE_PATH);
            if (folder.exists() && !folder.isDirectory()) {
                folder.delete();
            }
            if (!folder.exists()) {
                folder.mkdir();
            }

            // 保存配置文件
            IOUtils.writeText(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.FILE_PATH + Constants.FILE_NAME, gson.toJson(settingBean), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SharedPreferences getSp() {
        return getSharedPreferences(SP_NAME, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            int index = -1;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] >= 0) {
                    index = 0;
                }
            }
            if (index == 0) {
                init();
            }
        }
    }
}
