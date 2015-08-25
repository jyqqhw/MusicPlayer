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
			Toast.makeText(this, "δ��⵽����SD�����ڣ������SD��!", Toast.LENGTH_SHORT).show();
		}else{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			Log.i("aaa", "������oncreate_begin");
			setContentView(R.layout.activity_show);
			Log.i("aaa", "������oncreate_layout");
			init();//�����ʼ��

			File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "MediaPlayer",
					"music_list.xml");
			if(!file.exists()){
				file.getParentFile().mkdirs();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//�������л�����������������ƺ�·����Ϣ
				Serializer.XmlSerialize(GetList.getList(Environment.getExternalStorageDirectory()));
			}
			//����PULL�����õ��������ƺ�·����Ϣ
			listName = (ArrayList<String>) PullXml.PullName();
			listPath = (ArrayList<String>) PullXml.PullPath();	
			//}
			Log.i("aaa", "������oncreate_sd_mounted");

			Intent serviceIntent = new Intent(this,MusicService.class);
			startService(serviceIntent);
			Intent intent = new Intent(this,MusicService.class);
			bindService(intent, conn, 0);

			IntentFilter filter = new IntentFilter("com.az.mediaplayer.setprogressbar");
			registerReceiver(receiver, filter);//ע���϶������¹㲥
			filter = new IntentFilter("com.az.mediaplayer.currenttime	");
			registerReceiver(receiver, filter);//ע�ᵱǰ����ʱ����¹㲥
			filter = new IntentFilter("com.az.mediaplayer.settitlemaxalltime");
			registerReceiver(receiver, filter);//ע��������ƽ�������ʱ����ʾ��ʱ����¹㲥
			filter = new IntentFilter("com.az.mediaplayer.setplay");
			registerReceiver(receiver, filter);//ע��playͼ����¹㲥
			filter = new IntentFilter("com.az.mediaplayer.setpause");
			registerReceiver(receiver, filter);//ע��pauseͼ����¹㲥

			getPreference();
			if(MusicService.tojudgeisplaying == 0){
				firstIn = 1;//����ȫ�˳�������Ӧ�ó���
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
	 * �����ʼ��
	 */
	public void init(){
		Log.i("aaa", "������oncreate_init_begin");

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
		Log.i("aaa", "������oncreate_init_end");
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
	 * �õ��ϴβ��ŵĸ�����Ϣ
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
	 *�����㲥������ 
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
	 *SeekBar�����������ı�ļ����� 
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
	 *��ȡListActivity�ķ���ֵ
	 *onActivityResult����
	 * �ɵ�ǰActivity��������Activity�˳���ʱ����õķ��� 
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
	 * ���õ�������¼�����
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
			Toast.makeText(this, "û���˺Űɣ����㻹��¼��ɶ��", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_set:
			Toast.makeText(this, "���ܼܺ򵥣����������ˣ�лл��", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_exit:
			System.exit(0);
			break;

		}

	}
	/*
	 * onDestroy����
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
	 * ��ʾ������ǰʱ��ķ���
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
	 * ��ʾ������ʱ��ķ���
	 */
	public void ShowAllTime(int minute,int second){
		if(second<10){
			allTime.setText(minute+":0"+second);
		}else{
			allTime.setText(minute+":"+second);
		}
	}
	/*
	 * ����һ���߳�ˢ��������
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

