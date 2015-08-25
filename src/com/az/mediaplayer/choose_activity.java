package com.az.mediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class choose_activity extends Activity implements OnClickListener {
	
	private TextView detail,delete;
	private int position = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list_longclick);
		
		Intent intent = getIntent();
		position = intent.getIntExtra("position", -1);
		
		delete = (TextView) findViewById(R.id.delete);
		detail = (TextView) findViewById(R.id.detail);
		
		delete.setOnClickListener(this);
		detail.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.delete:
			Toast.makeText(this, "歌曲删除功能升级中...", Toast.LENGTH_SHORT).show();
			finish();
			break;
		case R.id.detail:
			Toast.makeText(this, "详情功能升级中...", Toast.LENGTH_SHORT).show();
			finish();
			break;
		}
		
	}
	
	
	
	
}
