package com.example.frida_gadget_manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.example.module.ModuleData;
import com.example.utils.LoadAppList;
import com.example.utils.MyLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "PersistListAppMainActivity_whitebird";
    ListView listModule = null;

    Button refreshAppList = null;

    List<ModuleData> dataList = new ArrayList<>();
    Context mContext = null;

    PersistListAppAdapter dataAdapter = null;

    public static HashMap<String, String> mAppJsPathMap = new HashMap<>();

    private void saveAppJsPath() {
        String jsonString = JSON.toJSONString(mAppJsPathMap);
        SharedPreferences sharedPreferences = getSharedPreferences("whitebird", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("JSPath", jsonString);
        MyLog.d(TAG, "saveAppJSPath  commit ");
        editor.commit();
    }

    //从 SharedPreferences 加载 JSON 字符串，将其解析为 Map 对象
    private void loadAppJsPath() {
        //模式为 0（对应于MODE_PRIVATE，意味着只有调用应用程序可以读取和写入它）
        SharedPreferences sharedPreferences = getSharedPreferences("whitebird", 0);
        //从 SharedPreferences 中检索存储在键“JSPath”下的 JSON 字符串。如果此键下没有存储任何值，则默认为空 JSON 对象（表示为字符串）。
        String tempString = sharedPreferences.getString("JSPath", JSON.toJSONString(new HashMap<>()));
        //将 JSON 字符串 ( tempString) 解析为 Map 对象 ( maps)。
        Map maps = (Map) JSON.parse(tempString);
        mAppJsPathMap.clear();
        mAppJsPathMap.putAll(maps);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_persisit_app_main);
        mContext = this;

        //授权外置内存卡读写权限
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            Toast.makeText(this, "授权读写外置卡成功", Toast.LENGTH_LONG).show();
        }

        loadAppJsPath();

        dataList = LoadAppList.loadAllInstalledApps(this);
        MyLog.d(TAG, "dataList ==> " + dataList.size());
        listModule = (ListView) findViewById(R.id.listViewInstalledListApp);

        dataAdapter = new PersistListAppAdapter(this, dataList, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (id == R.id.radioButtonFridaGadget) {
                    String methodType = "";
                    if (view.getId() == R.id.radioButtonFridaGadget) {
                        methodType = PersistSettings.WHITEBIRD_PERSIST;
                    }
                    boolean isActive = ((CheckBox) view).isChecked();
                    ModuleData moduleData = dataList.get((int) view.getTag());
                    MyLog.d(TAG, "moduleData:" + moduleData.uid + " " + moduleData.pkgName + "  isActive:" + isActive);
                    if (moduleData != null) {
                        // 先复制到 /data/system/xsettings/tmp/jscfg/pkgname/config.js
                        String jsPath = mAppJsPathMap.get(moduleData.pkgName);
                        if (jsPath == null) {
                            Toast.makeText(MainActivity.this, "未配置对应的js文件路径", Toast.LENGTH_SHORT).show();
                            refreshData();
                            return;
                        }
                        //创建/data/system/xsettings/tmp/jscfg/com.xxx.xxx/whitebird_persist
                        if (!PersistSettings.setEnablePersist(moduleData.pkgName, methodType, isActive)) {
                            Toast.makeText(MainActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
                            refreshData();
                            return;
                        }
                        ;
                        if (isActive) {
                            //关闭其勾选的
                            if (!methodType.equals(PersistSettings.WHITEBIRD_PERSIST)) {
                                //删除/data/system/xsettings/tmp/jscfg/com.xxx.xxx/whitebird_persist
                                PersistSettings.setEnablePersist(moduleData.pkgName, PersistSettings.WHITEBIRD_PERSIST, false);
                            }
                        }

                        if (isActive) {
                            //获取/data/system/xsettings/tmp/jscfg/pkgname/config.js
                            String pkgPath = PersistSettings.getAppJSPath(moduleData.pkgName);
                            //将hook脚本从sdcard/whitebird/下复制到/data/system/xsettings/tmp/jscfg/pkgname/config.js
                            if (PersistSettings.copyJSFileToAppJSPath(jsPath, pkgPath)) {
                                Toast.makeText(MainActivity.this, "配置成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
                            }
                            refreshData();
                        } else {
                            String appJsPath = PersistSettings.getAppJSPath(moduleData.pkgName);
                            File file = new File(appJsPath);
                            if (file.exists()) {
                                file.delete();
                            }
                            Toast.makeText(MainActivity.this, "停用成功", Toast.LENGTH_SHORT).show();
                            refreshData();
                        }
                    } else {
                        refreshData();
                        Toast.makeText(MainActivity.this, "未获取到配置的App信息", Toast.LENGTH_SHORT).show();
                    }
                } else if (id == R.id.buttonChooseJsFile) {
                    Map<String, Integer> images = new HashMap<>();
                    images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
                    images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
                    images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
                    images.put("wav", R.drawable.filedialog_wavfile);
                    images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
                    OpenFileDialog.createDialog(MainActivity.this, "文件选择", new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            String filepath = bundle.getString("path");
                            ModuleData moduleData = dataList.get((int) view.getTag());
                            MyLog.d(TAG, "packageName:" + moduleData.pkgName + " file path:" + filepath);

                            mAppJsPathMap.put(moduleData.pkgName, filepath);
                            saveAppJsPath();
                            refreshData();
                        }
                    }, ".js;", images).show();
                }
            }
        });
        listModule.setAdapter(dataAdapter);

        refreshAppList = (Button) findViewById(R.id.refreshAppList);
        refreshAppList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
            }
        });
    }

    private void refreshData() {
        dataList = LoadAppList.loadAllInstalledApps(mContext);
        dataAdapter.datas = dataList;
        dataAdapter.notifyDataSetChanged();
    }

}