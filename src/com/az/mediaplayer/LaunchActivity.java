package com.az.mediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class LaunchActivity extends Activity {
	private ImageView image = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.launch_activity);
		image = (ImageView) findViewById(R.id.image);
		AlphaAnimation anim = new AlphaAnimation(0.7f, 1.0f);
		anim.setDuration(3000	);
		image.startAnimation(anim);
		anim.setAnimationListener(new animListener());
		Log.i("aaa", "启动界面oncreate");
	}
	
	public class animListener implements AnimationListener{

		@Override
		public void onAnimationStart(Animation animation) {
			image.setBackgroundResource(R.drawable.zero);
			
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			Log.i("aaa", "启动界面oncreatefinish");
			startActivity(new Intent(LaunchActivity.this,ShowActivity.class));
			finish();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}
	
}
