package com.example.traveltimeplanner;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    // 用于选择地图API提供程序的Spinner
    private Spinner spinner_mapApiProvider;

    // 用于设置空余时间的SeekBar
    private SeekBar seekBar_customBufferTime;

    // 用于选择交通方式的Spinner
    private Spinner spinner_transportMode;

    // 用于设置直线距离阈值的EditText
    private EditText editText_distanceThreshold;

    // 用于设置活动状态颜色的EditText（假设以十六进制颜色值输入）
    private EditText editText_statusColor;

    // 用于设置提示内容的EditText
    private EditText editText_customPrompt;

    // 用于设置闹钟响铃时震动大小的RadioGroup
    private RadioGroup radioGroup_vibrationLevel;

    // 用于设置闹钟响铃是否启用声音及选择声音类型的RadioGroup
    private RadioGroup radioGroup_soundOption;

    // 用于输入地图API密钥的EditText
    private EditText editText_userInputMapApiKey;

    // 存储用户选择的地图API提供程序
    private String selectedMapApiProvider;

    // 存储用户设置的空余时间（分钟）
    private int customBufferTime;

    // 存储用户选择的交通方式
    private String selectedTransportMode;

    // 存储用户设置的直线距离阈值（千米）
    private double distanceThreshold;

    // 存储用户设置的活动状态颜色
    private int statusColor;

    // 存储用户设置的提示内容
    private String customPrompt;

    // 存储用户设置的闹钟响铃时震动大小（假设0为不震动，1为小震动，2为大震动）
    private int vibrationLevel;

    // 存储用户设置的闹钟响铃是否启用声音及选择声音类型（假设0为不启用，1为系统铃声，2为自定义音频文件）
    private int soundOption;

    // 存储用户输入的地图API密钥
    private String userInputMapApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化各个设置组件
        spinner_mapApiProvider = findViewById(R.id.spinner_mapApiProvider);
        seekBar_customBufferTime = findViewById(R.id.seekBar_customBufferTime);
        spinner_transportMode = findViewById(R.id.spinner_transportMode);
        editText_distanceThreshold = findViewById(R.id.editText_distanceThreshold);
        editText_statusColor = findViewById(R.id.editText_statusColor);
        editText_customPrompt = findViewById(R.id.editText_customPrompt);
        radioGroup_vibrationLevel = findViewById(R.id.radioGroup_vibrationLevel);
        radioGroup_soundOption = findViewById(R.id.radioGroup_soundOption);
        editText_userInputMapApiKey = findViewById(R.id.editText_userInputMapApiKey);

        // 设置地图API提供程序选择的下拉列表
        ArrayAdapter<CharSequence> mapApiAdapter = ArrayAdapter.createFromResource(this,
                R.array.map_api_providers, android.R.layout.simple_spinner_item);
        mapApiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mapApiProvider.setAdapter(mapApiAdapter);
        spinner_mapApiProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMapApiProvider = parent.getItemAtPosition(position).toString();
                Toast.makeText(SettingsActivity.this, "选择的地图API提供程序：" + selectedMapApiProvider, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 设置空余时间SeekBar的最大值和初始值（假设范围0 - 120分钟）
        seekBar_customBufferTime.setMax(120);
        seekBar_customBufferTime.setProgress(45);

        // 设置空余时间SeekBar的进度改变监听器
        seekBar_customBufferTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                customBufferTime = progress;
                Toast.makeText(SettingsActivity.this, "设置的空余时间：" + customBufferTime + "分钟", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 设置交通方式选择的下拉列表
        ArrayAdapter<CharSequence> transportModeAdapter = ArrayAdapter.createFromResource(this,
                R.array.transport_modes, android.R.layout.simple_spinner_item);
        transportModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_transportMode.setAdapter(transportModeAdapter);
        spinner_transportMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTransportMode = parent.getItemAtPosition(position).toString();
                Toast.makeText(SettingsActivity.this, "选择的交通方式：" + selectedTransportMode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 设置直线距离阈值EditText的文本改变监听器，更严格验证输入
        editText_distanceThreshold.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    try {
                        double value = Double.parseDouble(s.toString());
                        if (value < 0) {
                            Toast.makeText(SettingsActivity.this, "直线距离阈值不能为负数，请重新输入", Toast.LENGTH_SHORT).show();
                        } else {
                            distanceThreshold = value;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(SettingsActivity.this, "直线距离阈值请输入数字", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

// 设置活动状态颜色EditText的文本改变监听器，更严格验证输入
editText_statusColor.addTextWatcher(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().length() > 0 &&!s.toString().startsWith("#")) {
            Toast.makeText(SettingsActivity.this, "活动状态颜色请以#开头输入十六进制值", Toast.LENGTH_SHORT).show();
        } else if (s.toString().length() >= 3 && s.toString().length() <= 7 && TextUtils.isDigitsOnly(s.toString().substring(1))) {
            try {
                statusColor = Integer.parseInt(s.toString(), 16);
            } catch (NumberFormatException e) {
                Toast.makeText(SettingsActivity.this, "请输入有效的十六进制颜色值", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SettingsActivity.this, "请输入有效的十六进制颜色值", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
});

// 设置提示内容EditText的文本改变监听器
editText_customPrompt.addTextWatcher(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        customPrompt = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
});

// 设置闹钟响铃时震动大小RadioGroup的选择监听器
radioGroup_vibrationLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = findViewById(checkedId);
        if (radioButton!= null) {
            vibrationLevel = Integer.parseInt(radioButton.getText().toString());
            Toast.makeText(SettingsActivity.this, "设置的震动大小：" + vibrationLevel, Toast.LENGTH_SHORT).show();
        }
    }
});

// 设置闹钟响铃是否启用声音及选择声音类型RadioGroup的选择监听器
radioGroup_soundOption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = findViewById(checkedId);
        if (radioButton!= null) {
            soundOption = Integer.parseInt(radioButton.getText().toString());
            Toast.makeText(SettingsActivity.this, "设置的声音选项：" + soundOption, Toast.LENGTH_SHORT).show();
        }
    }
});

// 设置输入地图API密钥EditText的文本改变监听器
editText_userInputMapApiKey.addTextWatcher(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        userInputMapApiKey = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
});

// 当设置完成后，返回设置结果到MainActivity，包括API相关设置
public void onFinishSettings() {
    SettingsData settingsData = new SettingsData(
            selectedMapApiProvider,
            customBufferTime,
            selectedTransportMode,
            distanceThreshold,
            statusColor,
            customPrompt,
            vibrationLevel,
            soundOption,
            userInputMapApiKey
    );

    Intent intent = new Intent();
    intent.putExtra("settingsData", (Parcelable) settingsData);
    setResult(RESULT_OK, intent);
    finish();
}

// 定义可Parcelable的设置数据类，添加API相关属性
public static class SettingsData implements Parcelable {
    private String selectedMapApiProvider;
    private int customBufferTime;
    private String selectedTransportMode;
    private double distanceThreshold;
    private int statusColor;
    private String customPrompt;
    private int vibrationLevel;
    private int soundOption;
    private String userInputMapApiKey;

    public SettingsData(String selectedMapApiProvider, int customBufferTime, String selectedTransportMode,
                        double distanceThreshold, int statusColor, String customPrompt, int vibrationLevel,
                        int soundOption, String userInputMapApiKey) {
        this.selectedMapApiProvider = selectedMapApiProvider;
        this.customBufferTime = customBufferTime;
        this.selectedTransportMode = selectedTransportMode;
        this.distanceThreshold = distanceThreshold;
        this.statusColor = statusColor;
        this.customPrompt = customPrompt;
        this.vibrationLevel = vibrationLevel;
        this.soundOption = soundOption;
        this.userInputMapApiKey = userInputMapApiKey;
    }

    protected SettingsData(Parcel in) {
        selectedMapApiProvider = in.readString();
        customBufferTime = in.readInt();
        selectedTransportMode = in.readString();
        distanceThreshold = in.readDouble();
        statusColor = in.readInt();
        customPrompt = in.readString();
        vibrationLevel = in.readInt();
        soundOption = in.readInt();
        userInputMapApiKey = in.readString();
    }

    public static final Creator<SettingsData> CREATOR = new Creator<SettingsData>() {
        @Override
        public SettingsData createFromParcel(Parcel in) {
            return new SettingsData(in);
        }

        @Override
        public SettingsData[] newArray(int size) {
            return new SettingsData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selectedMapApiProvider);
        dest.writeInt(customBufferTime);
        dest.writeString(selectedTransportMode);
        dest.writeDouble(distanceThreshold);
        dest.writeInt(statusColor);
        dest.writeString(customPrompt);
        dest.writeInt(vibrationLevel);
        dest.writeInt(soundOption);
        dest.writeString(userInputMapApiKey);
    }

    // 以下是各个属性的getter方法，方便在MainActivity中获取设置数据
    public String getSelectedMapApiProvider() {
        return selectedMapApiProvider;
    }

    public int getCustomBufferTime() {
        return customBufferTime;
    }

    public String getSelectedTransportMode() {
        return selectedTransportMode;
    }

    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    public int getStatusColor() {
        return statusColor;
    }

    public String getCustomPrompt() {
        return customPrompt;
    }

    public int getVibrationLevel() {
        return vibrationLevel;
    }

    public int getSoundOption() {
        return soundOption;
    }

    public String getUserInputMapApiKey() {
        return userInputMapApiKey;
    }
}