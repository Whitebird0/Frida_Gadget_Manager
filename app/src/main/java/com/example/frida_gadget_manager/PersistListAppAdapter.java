package com.example.frida_gadget_manager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.module.ModuleData;

import java.util.List;


public class PersistListAppAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    public List<ModuleData> datas;
    Context mContext;

    View.OnClickListener onClickListener;

    public PersistListAppAdapter(Context context, List<ModuleData> datas) {
        super();
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.datas = datas;
    }

    public PersistListAppAdapter(Context context, List<ModuleData> datas, View.OnClickListener onClickListener) {
        super();
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.datas = datas;
        this.onClickListener = onClickListener;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    //返回与数据集中指定位置关联的行 ID
    @Override
    public long getItemId(int position) {
        return position;
    }
    //用于创建并返回数据集中特定位置的视图
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.app_installed_persist_list_item_layout, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.appInstalledIcon);
            holder.label = (TextView) convertView.findViewById(R.id.appInstalledName);
            holder.buttonConfigJsPath = (Button) convertView.findViewById(R.id.buttonChooseJsFile);
            holder.jsPath = (TextView) convertView.findViewById(R.id.textViewFridaInjectJsPath);
            holder.radioButtonGagdet=(CheckBox)convertView.findViewById(R.id.radioButtonFridaGadget);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setImageDrawable(datas.get(position).icon);
        holder.label.setText(datas.get(position).appName + " " + datas.get(position).versionName);

        String pkgName = datas.get(position).pkgName;
        String jsPath = MainActivity.mAppJsPathMap.get(pkgName);

        holder.jsPath.setTextColor(Color.RED);
        if (jsPath == null) {

            holder.jsPath.setText("未配置js文件");
        } else {
            holder.jsPath.setText("" + jsPath);
        }

        holder.label.setTag(position);
        holder.label.setOnClickListener(onClickListener);
        holder.buttonConfigJsPath.setOnClickListener(onClickListener);
        holder.buttonConfigJsPath.setTag(position);

        //radioButtonFridaGadget
        holder.radioButtonGagdet.setTag(position);
        holder.radioButtonGagdet.setChecked(datas.get(position).isWhitebird_Persist);
        holder.radioButtonGagdet.setOnClickListener(onClickListener);

        return convertView;
    }

    class ViewHolder {
        //app图标
        private ImageView icon;
        //app名字
        private TextView label;
        //frida 要hook的js地址
        private TextView jsPath;

        private CheckBox radioButtonGagdet;
        private Button buttonConfigJsPath;
    }
}
