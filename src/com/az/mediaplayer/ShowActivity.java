package com.az.mediaplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.az.mediaplayer.domain.MusicService;
import com.az.mediaplayer.domain.MusicService.MyBinder;
import com.az.mediaplayer.domain.PullXml;
import com.az.mediaplayer.domain.Serializer;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ShowActivity extends Activity implements OnClickListener {
	private String musicPath,musicName;
	private TextView currentTime,allTime,showTitle,menu_login,menu_set,menu_exit;
	private SeekBar seekBar;
	private int allMinute = 0,allSecond = 0,progressBarMax = 0,
			currentSecond = 0,currentProgress = 0,currentMinute = 0;

	private Button previous,play,next,to_list,to_left;
	public static ArrayList<String > listPath = null;
	public static ArrayList<String> listName = null;

	private int position = 0;
	public static int firstIn = 0;
	public static int firstSuperior = 0;

	private MyBinder myService;
	private SharedPreferences sp;
	private SlidingMenu sm;

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				ShowCurrentTime();
				seekBar.setProgress(currentProgress);
				showTitle.setText(musicName);
				seekBar.setMax(progressBarMax);
				ShowAllTime(allMinute, allSecond);
				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "未检测到内置SD卡存在，请插入SD卡!", Toast.LENGTH_SHORT).show();
		}else{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			Log.i("aaa", "主界面oncreate_begin");
			setContentView(R.layout.activity_show);
			Log.i("aaa", "主界面oncreate_layout");
			init();//程序初始化

			File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "MediaPlayer",
					"music_list.xml");
			if(!file.exists()){
				file.getParentFile().mkdirs();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//采用序列化方法保存歌曲的名称和路径信息
				Serializer.XmlSerialize(GetList.getList(Environment.getExternalStorageDirectory()));
			}
			//采用PULL解析得到歌曲名称和路径信息
			listName = (ArrayList<String>) PullXml.PullName();
			listPath = (ArrayList<String>) PullXml.PullPath();	
			//}
			Log.i("aaa", "主界面oncreate_sd_mounted");

			Intent serviceIntent = new Intent(this,MusicService.class);
			startService(serviceIntent);
			Intent intent = new Intent(this,MusicService.class);
			bindService(intent, conn, 0);

			IntentFilter filter = new IntentFilter("com.az.mediaplayer.setprogressbar");
			registerReceiver(receiver, filter);//注册拖动条更新广播
			filter = new IntentFilter("com.az.mediaplayer.currenttime	");
			registerReceiver(receiver, filter);//注册当前播放时间更新广播
			filter = new IntentFilter("com.az.mediaplayer.settitlemaxalltime");
			registerReceiver(receiver, filter);//注册歌曲名称进度条总时长显示总时间更新广播
			filter = new IntentFilter("com.az.mediaplayer.setplay");
			registerReceiver(receiver, filter);//注册play图标更新广播
			filter = new IntentFilter("com.az.mediaplayer.setpause");
			registerReceiver(receiver, filter);//注册pause图标更新广播

			getPreference();
			if(MusicService.tojudgeisplaying == 0){
				firstIn = 1;//（完全退出后）启动应用程序
			}else{
				if(MusicService.tojudgeplaypause == 0){
					play.setBackgroundResource(R.drawable.ic_media_play);
				}else if(MusicService.tojudgeplaypause == 1){
					play.setBackgroundResource(R.drawable.ic_media_pause);
				}

			}
			new UpdateUIThread().start();

		}

	}
	/*
	 * 程序初始化
	 */
	public void init(){
		Log.i("aaa", "主界面oncreate_init_begin");

		sm = new SlidingMenu(this);
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		sm.setMenu(R.layout.left_menu);

		menu_login = (TextView) findViewById(R.id.menu_login);
		menu_set = (TextView) findViewById(R.id.menu_set);
		menu_exit = (TextView) findViewById(R.id.menu_exit);

		to_left = (Button) findViewById(R.id.to_left);
		previous = (Button) findViewById(R.id.previous);
		play = (Button) findViewById(R.id.play);
		next = (Button) findViewById(R.id.next);
		currentTime = (TextView) findViewById(R.id.currentTime);
		allTime = (TextView) findViewById(R.id.allTime);
		showTitle = (TextView) findViewById(R.id.showTitle);

		to_list = (Button) findViewById(R.id.to_list);
		Log.i("aaa", "主界面oncreate_init_end");
		seekBar = (SeekBar) findViewById(R.id.seekBar);

		menu_login.setOnClickListener(this);
		menu_set.setOnClickListener(this);
		menu_exit.setOnClickListener(this);

		to_left.setOnClickListener(this);
		to_list.setOnClickListener(this);
		previous.setOnClickListener(this);
		play.setOnClickListener(this);
		next.setOnClickListener(this);
		seekBar.setOnSeekBarChangeListener(new SeekBarListener());
	}
	/*
	 * 得到上次播放的歌曲信息
	 */
	public void getPreference(){
		sp = getSharedPreferences("musicPreference", MODE_PRIVATE);
		int size = sp.getInt("size", -1);
		if(size != -1){
			if(size == listName.size()){
				position = sp.getInt("position", 0);
				musicPath = sp.getString("musicPath", null);
				musicName = sp.getString("musicName", null);
				//sp.getInt("currentMinute", 0);
				//sp.getInt("currentSecond", 0);
				allMinute = sp.getInt("allMinute", 0);
				allSecond = sp.getInt("allSecond", 0);
				progressBarMax = sp.getInt("progressBarMax", 0);
			}
		}
	}
	/*
	 *建立广播接收器 
	 */
	private BroadcastReceiver receiver = new  BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if("com.az.mediaplayer.currenttime".equals(action)){
				int minute = intent.getIntExtra("currentminute", 0);
				int second = intent.getIntExtra("currentsecond", 0);
				if(second<10){
					currentTime.setText(minute+":0"+second);
				}else{
					currentTime.setText(minute+":"+second);
				}
			}else if("com.az.mediaplayer.settitlemaxalltime".equals(action)){
				musicName = intent.getStringExtra("musicName");
				progressBarMax = intent.getIntExtra("max", 0);
				allMinute = intent.getIntExtra("allminute", 0);
				allSecond = intent.getIntExtra("allsecond", 0);
				showTitle.setText(musicName);
				seekBar.setMax(progressBarMax);
				ShowAllTime(allMinute, allSecond);
			}else if("com.az.mediaplayer.setplay".equals(action)){
				play.setBackgroundResource(R.drawable.ic_media_play);
			}else if("com.az.mediaplayer.setpause".equals(action)){
				play.setBackgroundResource(R.drawable.ic_media_pause);
			}	
		}
	};

	/*
	 * ServiceConnection
	 */
	private  ServiceConnection conn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myService = (MyBinder) service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	/*
	 *SeekBar进度条发生改变的监听器 
	 */
	public class SeekBarListener implements OnSeekBarChangeListener{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(firstIn == 1){
				myService.prepare();
				firstIn = 0;
			}
			int endProgress = seekBar.getProgress();
			myService.setCurrentProgress(endProgress);
		}
	}
	/*
	 *获取ListActivity的返回值
	 *onActivityResult方法
	 * 由当前Activity开启的新Activity退出的时候调用的方法 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
			position = data.getIntExtra("position", 0);
			listPath = (ArrayList<String>) PullXml.PullPath();
			myService.setPosition(position);
			myService.ToPlay(listPath.get(position));
			firstSuperior = 1;
			play.setBackgroundResource(R.drawable.ic_media_pause);
		}
	}
	/*
	 * 设置点击监听事件方法
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if(firstIn == 1&&firstSuperior != 1){
			myService.setPosition(position);
			myService.prepare();
			firstIn = 0;
		}
		Intent intent;
		switch (v.getId()) {
		case R.id.previous:
			myService.ToPlayPrevious();
			break;
		case R.id.play:
			myService.ToPlayPause();
			break;
		case R.id.next:
			myService.ToPlayNext();
			break;
		case R.id.to_list:
			intent = new Intent(this,ListActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.to_left:
			sm.toggle();
			break;
		case R.id.menu_login:
			Toast.makeText(this, "没有账号吧？那你还登录个啥！", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_set:
			Toast.makeText(this, "功能很简单，不用设置了，谢谢！", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_exit:
			System.exit(0);
			break;

		}

	}
	/*
	 * onDestroy方法
	 */
	@Override
	protected void onDestroy(){
		position = myService.getPosition();
		myService.setJudge(1);
		sp = getSharedPreferences("musicPreference", MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("size", listName.size());
		editor.putInt("position", position);
		editor.putString("musicPath", listPath.get(position));
		editor.putString("musicName", musicName);
		editor.putInt("currentProgress", currentProgress);
		editor.putInt("currentMinute", currentMinute);
		editor.putInt("currentSecond", currentSecond);
		editor.putInt("allMinute", allMinute);
		editor.putInt("allSecond", allSecond);
		editor.putInt("progressBarMax", progressBarMax);
		editor.commit();
		try {
			unbindService(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}
	/*
	 * 显示歌曲当前时间的方法
	 */
	public void ShowCurrentTime(){
		currentProgress = myService.GetCurrentProgress();
		int nowTime = currentProgress/1000;
		currentMinute = nowTime/60;
		currentSecond = nowTime%60;
		if(currentSecond<10){
			currentTime.setText(currentMinute+":0"+currentSecond);
		}else{
			currentTime.setText(currentMinute+":"+currentSecond);
		}
	}
	/*
	 * 显示歌曲总时间的方法
	 */
	public void ShowAllTime(int minute,int second){
		if(second<10){
			allTime.setText(minute+":0"+second);
		}else{
			allTime.setText(minute+":"+second);
		}
	}
	/*
	 * 建立一个线程刷新主界面
	 */
	private class UpdateUIThread extends Thread{

		public void run() {
			while(true){
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(100);
			}
		}
	}


}

