package com.example.traveltimeplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureTaskListener;
import com.google.android.gms.tasks.OnSuccessTaskListener;
import com.google.android.gms.speech.RecognitionResult;
import com.google.android.gms.speech.SpeechRecognizer;
import com.google.android.gms.speech.SpeechRecognitionResult;
import com.google.android.gms.speech.RecognizerIntent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // UI组件
    private EditText editText_departureTime;
    private EditText editText_arrivalTime;
    private EditText editText_stationLocation;
    private Button button_submit;
    private Button button_settings;
    private TextView textView_travelTime;
    private TextView textView_status;
    private Spinner spinner_transportMode;
    private EditText editText_userInputMapApiKey;
    private Spinner spinner_userInputMapApiProvider;

    // 权限相关常量
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
    };

    // 时间格式常量
    private static final String TIME_FORMAT = "HH:mm";

    // 用于存储出发时间、到达时间等信息
    private Date departureTime;
    private Date arrivalTime;

    // 定时器相关
    private Timer timer;

    // 用于存储当前位置（A）
    private Location currentLocation;

    // 存储用户选择的交通方式
    private String selectedTransportMode;

    // 存储交通方式对应的最长交通用时（分钟）
    private int maxTravelTime;

    // 存储用户自定义的空余时间（分钟）
    private int customBufferTime;

    // 存储用户自定义的直线距离阈值（千米）
    private double distanceThreshold;

    // 存储用户自定义的活动状态颜色
    private int statusColor;

    // 存储用户自定义的提示内容
    private String customPrompt;

    // 存储闹钟响铃时是否震动及震动大小（假设0为不震动，1为小震动，2为大震动）
    private int vibrationLevel;

    // 存储是否启用声音及声音选择（假设0为不启用，1为系统铃声，2为自定义音频文件）
    private int soundOption;

    // 用于存储用户输入的地图API密钥
    private String userInputMapApiKey;

    // 用于存储用户选择的地图API提供程序（假设可自定义输入，这里可能是具体的API服务名称等）
    private String userInputMapApiProvider;

    // 语音识别器
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI组件
        editText_departureTime = findViewById(R.id.editText_departureTime);
        editText_arrivalTime = findViewById(R.id.editText_arrivalTime);
        editText_stationLocation = findViewById(R.id.editText_stationLocation);
        button_submit = findViewById(R.id.button_submit);
        button_settings = findViewById(R.id.button_settings);
        textView_travelTime = findViewById(R.id.textView_travelTime);
        textView_status = findViewById(R.id.textView_status);
        spinner_transportMode = findViewById(R.id.spinner_transportMode);
        editText_userInputMapApiKey = findViewById(R.id.editText_userInputMapApiKey);
        spinner_userInputMapApiProvider = findViewById(R.id.spinner_userInputMapApiProvider);

        // 设置交通方式选择的下拉列表
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transport_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_transportMode.setAdapter(adapter);
        spinner_transportMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTransportMode = parent.getItemAtPosition(position).toString();
                // 根据选择的交通方式获取最长交通用时（这里假设是通过某种方式查询到的，实际需完善）
                maxTravelTime = getMaxTravelTime(selectedTransportMode);
                textView_travelTime.setText("预计最长交通用时：" + maxTravelTime + "分钟");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 请求必要权限
        requestPermissionsIfNeeded();

        // 设置提交按钮点击事件
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取输入信息
                String departureTimeStr = editText_departureTime.getText().toString().trim();
                String arrivalTimeStr = editText_arrivalTime.getText().toString().trim();
                String stationLocation = editText_stationLocation.getText().toString().trim();

                // 获取用户输入的地图API相关信息
                userInputMapApiKey = editText_userInputMapApiKey.getText().toString().trim();
                userInputMapApiProvider = spinner_userInputMapApiProvider.getSelectedItem().toString();

                // 验证输入是否符合时间格式
                if (!isValidTimeFormat(departureTimeStr) ||!isValidTimeFormat(arrivalTimeStr)) {
                    Toast.makeText(MainActivity.this, "出发时间和到达时间格式不正确，请重新输入（格式：" + TIME_FORMAT + "）", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 验证车站位置是否为空
                if (TextUtils.isEmpty(stationLocation)) {
                    Toast.makeText(MainActivity.this, "车站位置不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 验证地图API相关信息是否完整
                if (TextUtils.isEmpty(userInputMapApiKey) || TextUtils.isEmpty(userInputMapApiProvider)) {
                    Toast.makeText(MainActivity.this, "请输入完整的地图API信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
                    departureTime = sdf.parse(departureTimeStr);
                    arrivalTime = sdf.parse(arrivalTimeStr);

                    // 获取当前位置（A）
                    if (currentLocation == null) {
                        Toast.makeText(MainActivity.this, "尚未获取到当前位置，请稍后再试", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 接入地图API计算当前位置（A）到车站位置的时间（这里由用户自定义API，实际调用需用户自行实现）
                    // 示例代码省略具体API调用，仅保留逻辑框架
                    PlanNode startNode = PlanNode.withLocation(getLatLngFromLocation(currentLocation));
                    PlanNode endNode = PlanNode.withLocation(getLatLngFromLocation(stationLocation));

                    // 根据用户选择的API提供程序和输入的密钥进行地图API的初始化和路线搜索（由用户自行实现具体API调用）
                    if (!TextUtils.isEmpty(userInputMapApiKey) &&!TextUtils.isEmpty(userInputMapApiProvider)) {
                        // 用户需在此处根据所选API提供程序和输入的密钥进行具体的地图API初始化和路线搜索操作
                        // 例如，若选择百度地图API，可能需要类似如下代码（实际需根据百度地图API具体要求调整）：
                        // BaiduMapOptions options = new BaiduMapOptions();
                        // options.locationMode(BaiduMap.LocationMode.HIGH_ACCURACY);
                        // options.setMyLocationEnabled(true);
                        // options.apiKey(userInputMapApiKey);
                        // mapView = new MapView(MainActivity.this, options);
                        // setContentView(mapView);
                        // baiduMap = mapView.getMap();
                        // routePlanSearch = RoutePlanSearch.newInstance();
                        // routePlanSearch.setOnGetRoutePlanResultListener(MainActivity.this);
                        // 根据不同交通方式进行路线搜索
                        if (selectedTransportMode.equals("步行")) {
                            // routePlanSearch.walkingRouteSearch(new WalkingRouteResult());
                        } else if (selectedTransportMode.equals("驾车")) {
                            // routePlanSearch.drivingRouteSearch(new DrivingRouteResult());
                        } else if (selectedTransportMode.equals("公共交通")) {
                            // routePlanSearch.massTransitRouteSearch(new MassTransitRouteResult());
                        }
                    }

                    // 设置出发闹钟时间
                    setDepartureAlarmTime();

                    // 暂时先模拟设置旅行时间显示
                    textView_travelTime.setText("模拟旅行时间：2小时");
                } catch (java.text.ParseException e) {
                    Toast.makeText(MainActivity.this, "时间格式转换错误，请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置打开设置页面按钮点击事件
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            }
        });

        // 初始化语音识别器
        speechRecognizer = SpeechRecognizer.create(this);

        // 设置语音输入按钮点击事件（假设新增一个按钮用于语音输入）
        Button button_voiceInput = findViewById(R.id.button_voiceInput);
        button_voiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, VOICE_INPUT_REQUEST_CODE);
                } else {
                    startVoiceInput(speechRecognizer);
                }
            }
        });

        // 初始化位置跟踪（假设在Activity创建时就开始跟踪）
        startLocationTracking();

        // 设置UI组件的可访问性描述
        setAccessibilityDescriptions();
    }

    // 验证时间格式的方法
    private boolean isValidTimeFormat(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(time);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }

    // 请求必要权限的方法
    private void requestPermissionsIfNeeded() {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)!= PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    // 处理权限请求结果的方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result!= PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, "部分权限未授予，可能影响部分功能", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == VOICE_INPUT_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput(SpeechRecognizer.create(this));
            } else {
                Toast.makeText(this, "语音输入权限未授予", Toast.LENGTH_SHORT).show();
            }
        }

        // 针对权限被拒情况，提供更清晰的引导重新获取权限
        if (!allGranted) {
            if (requestCode == PERMISSION_REQUEST_CODE && permissions.length > 0 && permissions[0].equals(Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "语音输入功能需要录音权限，请在设置中重新授予此权限", Toast.LENGTH_LONG).show();
                // 可添加引导用户到设置页面重新授权的逻辑，例如：
                // Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                // Uri uri = Uri.fromParts("package", getPackageName(), null);
                // intent.setData(uri);
                // startActivity(intent);
            } else if (requestCode == PERMISSION_REQUEST_CODE && permissions.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "应用部分功能需要位置权限，请在设置中重新授予此权限", Toast.LENGTH_LONG).show();
                // 同样可添加引导用户到设置页面重新授权的逻辑
            }
        }
    }

    // 启动定时器的方法，更新为可更新闹钟时间的逻辑
    private void setDepartureAlarmTime() {
        if (timer!= null) {
            timer.cancel();
        }
        timer = new Timer();
        long departureAlarmTime = departureTime.getTime() - ((maxTravelTime + customBufferTime) * 60 * 1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, customPrompt + "出发啦！", Toast.LENGTH_LONG).show();
                        // 出发闹钟响后，计算当前位置（B）到之前位置（A）的直线距离
                        Location currentLocationB = getCurrentLocation();
                        if (currentLocationB!= null && currentLocation!= null) {
                            double distance = getDistanceBetweenLocations(currentLocation, currentLocationB);
                            if (distance > distanceThreshold) {
                                textView_status.setTextColor(Color.GREEN);
                                textView_status.setText("请注意时间");
                            } else {
                                textView_status.setTextColor(Color.RED);
                                textView_status.setText("请抓紧时间");
                            }
                        }
                    }
                });
            }
        }, new Date(departureAlarmTime), 0);
    }

    // 开始语音输入的方法
    private void startVoiceInput(SpeechRecognizer speechRecognizer) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出出发时间、到达时间或车站位置");

        Task<RecognitionResult> task = speechRecognizer.recognize(intent);
        task.addOnSuccessTaskListener(new OnSuccessTaskListener<RecognitionResult>() {
            @Override
            public Task<Void> onSuccess(RecognitionResult result) {
                String spokenText = result.getAlternatives().get(0).getText();

                // 根据语音识别结果判断并填充到相应输入框
                if (spokenText.contains("出发时间")) {
                    String[] parts = spokenText.split(":");
                    if (parts.length >= 2) {
                        editText_departureTime.setText(parts[1].trim());
                    }
                } else if (spokenText.contains("到达时间")) {
                    String[] parts = spokenText.split(":");
                    if (parts.length >= 2) {
                        editText_arrivalTime.setText(parts[1].trim());
                    }
                } else if (spokenText.contains("车站位置") && spokenText.indexOf("车站位置") == 0) {
                    editText_stationLocation.setText(spokenText.substring(5).trim());
                }

                Toast.makeText(MainActivity.this, "识别结果：" + spokenText, Toast.LENGTH_SHORT).show();
                return null;
            }
        });
        task.addOnFailureTaskListener(new OnFailureTaskListener() {
            @Override
            public Task<Void> onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "语音识别失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        });
    }

    // 初始化位置跟踪的方法，优化位置获取精度和稳定性
    private void startLocationTracking() {
        // 设置更精确的位置更新参数，这里由用户根据所选地图API自行配置（示例省略具体API相关设置）
        // 例如
// 例如，若使用百度地图API，可能需如下设置（实际需根据具体API及用户输入调整）
// BaiduMapOptions options = new BaiduMapOptions();
// options.locationMode(BaiduMap.LocationMode.HIGH_ACCURACY);
// options.setMyLocationEnabled(true);
// options.apiKey(userInputMapApiKey);
// mapView = new MapView(this, options);

// setContentView(mapView);

// baiduMap = mapView.getMap();
// baiduMap.setOnMyLocationChangeListener(new BaiduMap.OnMyLocationChangeListener() {
//     @Override
//     public void onMyLocationChange(Location location) {
//         currentLocation = location;
//         Toast.makeText(MainActivity.this, "当前位置（A）已更新：" + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//     }
// });

// 这里暂时只给出位置获取的示意，实际需根据用户自定义API完成准确设置和获取
LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            Toast.makeText(MainActivity.this, "当前位置（A）已更新：" + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    });
}

// 根据位置获取LatLng对象的方法（简单示例，实际可能需要更精确的解析，且可能依赖用户所选地图API）
private LatLng getLatLngFromLocation(Location location) {
    return new LatLng(location.getLatitude(), location.getLongitude());
}

// 获取用户选择的交通方式的最长交通用时的方法（这里暂时简单返回示例值，实际需完善查询逻辑）
private int getMaxTravelTime(String transportMode) {
    if (transportMode.equals("步行")) {
        return 60;
    } else if (transportMode.equals("驾车")) {
        return 30;
    } else if (transportMode.equals("公共交通")) {
        return 90;
    }
    return 0;
}

// 获取当前位置（B）的方法（这里假设通过某种方式获取到最新位置，实际需完善，且可能与地图API相关）
private Location getCurrentLocation() {
    // 这里可以使用用户自定义的地图API或者其他定位方式获取最新位置，暂时返回null示例
    return null;
}

// 计算两个位置之间直线距离的方法（单位：千米），可使用更精确的地理计算库或算法（也可能与地图API相关）
private double getDistanceBetweenLocations(Location locationA, Location locationB) {
    if (locationA!= null && locationB!= null) {
        float[] results = new float[1];
        Location.distanceBetween(locationA.getLatitude(), locationA.getLongitude(), locationB.getLatitude(), locationB.getLongitude(), results);
        return results[0] / 1000.0;
    }
    return 0;
}

// 设置上车闹钟的方法，更新为可更新闹钟时间的逻辑
private void setBoardingAlarmTime() {
    if (timer!= null) {
        timer.cancel();
    }
    timer = new Timer();
    long boardingAlarmTime = departureTime.getTime() - (30 * 60 * 1000);
    timer.schedule(new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, customPrompt + "请注意上车", Toast.LENGTH_LONG).show();
                    textView_status.setTextColor(Color.YELLOW);
                    textView_status.setText("请注意上车");
                }
            });
        }
    }, new Date(boardingAlarmTime), 0);
}

// 设置下车闹钟的方法，更新为可更新闹钟时间的逻辑
private void setAlightingAlarmTime() {
    if (timer!= null) {
        timer.cancel();
    }
    timer = new Timer();
    long alightingAlarmTime = arrivalTime.getTime() - (30 * 60 * 1000);
    timer.schedule(new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, customPrompt + "请注意下车", Toast.LENGTH_LONG).show();
                    textView_status.setTextColor(Color.BLACK);
                    textView_status.setText("请注意下车");
                }
            });
        }
    }, new Date(alightingAlarmTime), 0);
}

// 处理从SettingsActivity返回的结果，全面更新相关业务逻辑
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
        // 获取自定义设置参数
        customBufferTime = data.getIntExtra("customBufferTime", 45);
        selectedTransportMode = data.getStringExtra("selectedTransportMode");
        distanceThreshold = data.getDoubleExtra("distanceThreshold", 1.0);
        statusColor = data.getIntExtra("statusColor", Color.GREEN);
        customPrompt = data.getStringExtra("customPrompt");
        vibrationLevel = data.getIntExtra("vibrationLevel", 0);
        soundOption = data.getIntExtra("soundOption", 0);

        // 获取用户更新的地图API相关设置
        userInputMapApiKey = data.getStringExtra("userInputMapApiKey");
        userInputMapApiProvider = data.getStringExtra("userInputMapApiProvider");

        // 根据新的交通方式重新计算最长交通用时
        maxTravelTime = getMaxTravelTime(selectedTransportMode);
        textView_travelTime.setText("预计最长交通用时：" + maxTravelTime + "分钟");

        // 根据新的API设置重新配置地图相关操作（由用户自行根据所选API实现具体配置）
        if (!TextUtils.isEmpty(userInputMapApiKey) &&!TextUtils.isEmpty(userInputMapApiProvider)) {
            // 示例：若为百度地图API，可能需如下代码（实际需按百度地图API具体要求及用户输入调整）
            // BaiduMapOptions options = new BaiduMapOptions();
            // options.locationMode(BaiduMap.LocationMode.HIGH_ACCURACY);
            // options.setMyLocationEnabled(true);
            // options.apiKey(userInputMapApiKey);
            // mapView = new MapView(MainActivity.this, options);

            // setContentView(mapView);

            // baiduMap = mapView.getMap();
            // routePlanSearch = RoutePlanSearch.newInstance();
            // routePlanSearch.setOnGetRoutePlanResultListener(MainActivity.this);

            // 根据新的交通方式重新计算路线（以当前位置（A）到车站位置为例）
            if (currentLocation!= null) {
                String stationLocation = editText_stationLocation.getText().toString().trim();
                PlanNode startNode = PlanNode.withLocation(getLatLngFromLocation(currentLocation));
                PlanNode endNode = PlanNode.withLocation(getLatLngFromLocation(stationLocation));
                if (selectedTransportMode.equals("步行")) {
                    // routePlanSearch.walkingRouteSearch(new WalkingRouteResult());
                } else if (selectedTransportMode.equals("驾车")) {
                    // routePlanSearch.drivingRouteSearch(new DrivingRouteResult());
                } else if (selectedTransportMode.equals("公共交通")) {
                    // routePlanSearch.massTransitRouteSearch(new MassTransitRouteResult());
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "请输入完整的地图API信息", Toast.LENGTH_SHORT).show();
        }

        // 更新闹钟时间
        setDepartureAlarmTime();
        setBoardingAlarmTime();
        setAlightingAlarmTime();

        // 更新UI显示颜色等
        textView_status.setTextColor(statusColor);

        Toast.makeText(this, "已应用自定义设置", Toast.LENGTH_SHORT).show();
    }
}

// 设置UI组件的可访问性描述
private void setAccessibilityDescriptions() {
    button_submit.setContentDescription("提交输入的车票信息");
    button_settings.setContentDescription("打开设置页面");
    textView_travelTime.setContentDescription("显示预计最长交通用时");
    textView_status.setContentDescription("显示活动状态提示");
    spinner_transportMode.setContentDescription("选择出行的交通方式");
    button_voiceInput.setContentDescription("进行语音输入");
    editText_userInputMapApiKey.setContentDescription("输入地图API密钥");
    spinner_userInputMapApiProvider.setContentDescription("选择地图API提供程序");
}

// 自定义请求码常量
private static final int SETTINGS_REQUEST_CODE = 1001;
private static final int VOICE_INPUT_REQUEST_CODE = 200;

@Override
protected void onResume() {
    super.onResume();
    // 若使用地图视图（如MapView），需在此处恢复相关操作，示例如下（实际需根据具体地图API及使用情况调整）
    // if (mapView!= null) {
    //     mapView.onResume();
    // }
}

@Override
protected void onPause() {
    super.onPause();
    // 若使用地图视图（如MapView），需在此处暂停相关操作，示例如下（实际需根据具体地图API及使用情况调整）
    // if (mapView!= null) {
    //     mapView.onPause();
    // }
}

@Override
protected void onDestroy() {
    super.onDestroy();
    // 若使用地图视图（如MapView），需在此处销毁相关操作，示例如下（实际需根据具体地图API及使用情况调整）
    // if (mapView!= null) {
    //     mapView.onDestroy();
    // }
    // 取消定时器，避免内存泄漏等问题
    if (timer!= null) {
        timer.cancel();
    }
    // 释放语音识别器资源
    if (speechRecognizer!= null) {
        speechRecognizer.destroy();
    }
}
