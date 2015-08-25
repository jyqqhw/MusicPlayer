package com.az.mediaplayer.domain;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.az.mediaplayer.ShowActivity;

public class MusicService extends Service {
	private int position;
	public static int tojudgeplaypause = -1;
	public static int tojudgeisplaying = 0;
	public ArrayList<String > listPath;
	public ArrayList<String> listName;
	/*
	 * 单例化MediaPlayer
	 */
	private  static final MediaPlayer mediaPlayer = new MediaPlayer();
	public  static MediaPlayer getPlayer(){
		return mediaPlayer;
	}

	@Override
	public IBinder onBind(Intent intent) {
		MyBinder myBinder = new MyBinder();
		return myBinder;
	}

	public class MyBinder extends Binder{
		public void setJudge(int judge){
			tojudgeisplaying = judge;
		}
		public void setPosition(int pos){
			position = pos;
		}
		public int getPosition(){
			return position;
		}
		public void ToPlay(String path){
			Play(path);
		}
		public void ToPlayPrevious(){
			PlayPrevious();
		}
		public void ToPlayNext(){
			PlayNext();
		}
		public void ToPlayPause(){
			PlayPause();
		}
		public int GetDuration() {
			return mediaPlayer.getDuration();
		}
		public int GetCurrentProgress(){
			if(ShowActivity.firstIn != 1){
				return mediaPlayer.getCurrentPosition();
			}else{
				return 0;
			}
		}
		public void setCurrentProgress(int progress){
			mediaPlayer.seekTo(progress);
		}
		public void prepare(){
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mediaPlayer.setDataSource(listPath.get(position));
				mediaPlayer.prepare();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onCreate() {
		listName = (ArrayList<String>) PullXml.PullName();
		listPath = (ArrayList<String>) PullXml.PullPath();
		mediaPlayer.setOnCompletionListener(new CompleteListener());
	}
	/*
	 * onstartconmmand
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		Notification notification = new Notification(R.drawable.title, "实例", System.currentTimeMillis());
		Intent intentnotification = new Intent(this,ListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentnotification, 0);
		notification.setLatestEventInfo(this, "音乐播放器",listName.get(position), pendingIntent);
		notification.flags = Notification.FLAG_INSISTENT;
		startForeground(0x12345, notification);
		 */
		return super.onStartCommand(intent, flags, startId);
	}
	/*
	 * 设置歌曲播放完成监听事件方法
	 */
	public class CompleteListener implements OnCompletionListener{
		@Override
		public void onCompletion(MediaPlayer mp) {
			Toast.makeText(MusicService.this, "播放下一曲", Toast.LENGTH_SHORT).show();
			PlayNextAuto();
		}
	}
	/*
	 * 播放一首音乐
	 */
	public void Play(String path){
		listName = (ArrayList<String>) PullXml.PullName();
		listPath = (ArrayList<String>) PullXml.PullPath();
		if(mediaPlayer.isPlaying()){
			mediaPlayer.reset();
		}
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
		int max = mediaPlayer.getDuration();//显示当前歌曲进度条总时长
		int allTime = mediaPlayer.getDuration()/1000;
		int minute = allTime/60;
		int second = allTime%60;
		Intent intent = new Intent("com.az.mediaplayer.settitlemaxalltime");
		intent.putExtra("musicName", listName.get(listPath.indexOf(path)));//当前歌曲名称
		intent.putExtra("allminute", minute);//当前歌曲分钟
		intent.putExtra("allsecond", second);//当前歌曲秒钟
		intent.putExtra("max", max);//当前进度条总时长
		sendBroadcast(intent);
	}
	/*
	 * 停止播放音乐
	 */
	public void Stop(){
		mediaPlayer.stop();
		mediaPlayer.release();
	}
	/*
	 *自动播放下一首歌 
	 */
	public void PlayNextAuto(){
		mediaPlayer.reset();
		if(position < listPath.size()-1){
			position = position + 1;
			Play(listPath.get(position));
		}else{
			position =0;
			Play(listPath.get(position));
		}
	}
	/*
	 * 点击播放下一首音乐
	 */
	public void PlayNext(){
		int flag = 0;
		if(mediaPlayer.isPlaying()){
			flag = 1;
			Intent intent = new Intent("com.az.mediaplayer.setpause");
			sendBroadcast(intent);
			//play.setBackgroundResource(R.drawable.control_pause);
		}
		mediaPlayer.reset();
		if(position < listPath.size()-1){
			position = position + 1;
			Play(listPath.get(position));
		}else{
			position =0;
			Play(listPath.get(position));
		}
		if(flag == 0){
			mediaPlayer.pause();
			Intent intent = new Intent("com.az.mediaplayer.setplay");
			sendBroadcast(intent);
			//play.setBackgroundResource(R.drawable.control_play);
		}
	}
	/*
	 * 播放和暂停
	 */
	public void PlayPause(){
		if(mediaPlayer.isPlaying()){
			mediaPlayer.pause();
			tojudgeplaypause = 0;
			Intent intent = new Intent("com.az.mediaplayer.setplay");
			sendBroadcast(intent);
			//play.setBackgroundResource(R.drawable.control_play);
		}else if(!mediaPlayer.isPlaying()){
			mediaPlayer.start();
			tojudgeplaypause = 1;
			Intent intent = new Intent("com.az.mediaplayer.setpause");
			sendBroadcast(intent);
			//play.setBackgroundResource(R.drawable.control_pause);
		}
	}
	/*
	 * 播放上一首音乐
	 */
	public void PlayPrevious(){
		int flag = 0;
		if(mediaPlayer.isPlaying()){
			flag = 1;
			Intent intent = new Intent("com.az.mediaplayer.setpause");
			sendBroadcast(intent);
			//play.setBackgroundResource(R.drawable.control_pause);
		}
		mediaPlayer.reset();
		if(position > 0){
			position = position - 1;
			Play(listPath.get(position));
		}else{
			position = listPath.size()-1;
			Play(listPath.get(position));
		}
		if(flag == 0){
			mediaPlayer.pause();
		}
	}
	/*
	 * 服务被销毁
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		//stopForeground(true);
	}

}
