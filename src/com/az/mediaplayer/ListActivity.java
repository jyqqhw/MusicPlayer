package com.az.mediaplayer;

import java.util.ArrayList;
import java.util.zip.Inflater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.az.mediaplayer.domain.PullXml;
import com.az.mediaplayer.domain.Serializer;


public class ListActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {

	private ListView listview;
	private ListAdapter myAdapter;
	public static ArrayList<String > listPath = null;
	public static ArrayList<String> listName = null;
	private Button refresh;
	private LinearLayout ll;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 111:
				if(listName != null){
					myAdapter = new ArrayAdapter<String>(ListActivity.this, android.R.layout.simple_list_item_1,listName);
					listview.setAdapter(myAdapter);
					listview.setOnItemClickListener(ListActivity.this);
					ll.setVisibility(View.GONE);
					listview.setVisibility(View.VISIBLE);
					refresh.setText("刷新列表");
					refresh.setEnabled(true);
					refresh.setTextColor(Color.BLACK);
					Toast.makeText(ListActivity.this, "更新播放列表成功", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(ListActivity.this, "播放列表为空", Toast.LENGTH_SHORT).show();
				}
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		listview = (ListView) findViewById(R.id.listview);
		refresh = (Button) findViewById(R.id.refresh);
		ll = (LinearLayout) findViewById(R.id.ll);

		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "未检测到SD卡存在，请插入SD卡!", Toast.LENGTH_SHORT).show();
		}else{
			//采用PULL解析得到歌曲名称信息
			listName = (ArrayList<String>) PullXml.PullName();
			if(listName != null){
				myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listName);
				listview.setAdapter(myAdapter);
				listview.setOnItemClickListener(this);
				listview.setOnItemLongClickListener(this);
				Toast.makeText(this, "获取播放列表成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
			}
		}

	}
	/*
	 * 点击刷新列表方法
	 */
	public void Refresh(View view){
		refresh.setText("刷新中...");
		refresh.setTextColor(Color.GRAY);
		refresh.setEnabled(false);
		listview.setVisibility(View.GONE);
		ll.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				listName = GetList.getName();
				//采用序列化方法保存歌曲的名称和路径信息
				Serializer.XmlSerialize(GetList.getList(Environment.getExternalStorageDirectory()));
				handler.sendEmptyMessage(111);
			};
		}.start();

	}
	/*
	 * 列表项被点击事件的方法
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> pnarent, View view, int position,
			long id) {
		Intent data = new Intent();
		data.putExtra("position", position);
		setResult(0, data);
		finish();
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(this, choose_activity.class);
		intent.putExtra("position", position);
		startActivity(intent);
		return false;
	}
	/*
	 * 自定义适配器
	 */
	/*
	public class mySimpleAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listName.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listName.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(ListActivity.this).inflate(R.layout.music_list, null);
			return view;
		}
		
	}
	*/

}
