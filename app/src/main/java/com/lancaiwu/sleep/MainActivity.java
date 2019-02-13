package com.lancaiwu.sleep;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.lancaiwu.sleep.bean.SettingBean;
import com.lancaiwu.sleep.bean.TimeBean;
import com.lancaiwu.sleep.utils.Constants;
import com.lancaiwu.sleep.utils.IOUtils;

import java.io.File;

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

        saveSetting();
    }

    /**
     * 初始化 数据
     */
    private void initData() {
        // 文件里面没有东西，读取sp里面的数据
        settingBean = new SettingBean();
        SharedPreferences sharedPreferences = getSp();
        settingBean.setEnable(sharedPreferences.getBoolean("isEnable", false));

        TimeBean startTimeBean = new TimeBean();
        startTimeBean.setHour(sharedPreferences.getInt("startTime_hour", 23));
        startTimeBean.setMinute(sharedPreferences.getInt("startTime_minute", 0));

        settingBean.setStartTime(startTimeBean);

        TimeBean endTimeBean = new TimeBean();

        endTimeBean.setHour(sharedPreferences.getInt("endTime_hour", 6));
        endTimeBean.setMinute(sharedPreferences.getInt("endTime_minute", 0));
        settingBean.setEndTime(endTimeBean);

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