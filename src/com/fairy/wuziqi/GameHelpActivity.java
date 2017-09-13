package com.fairy.wuziqi;

import android.os.Bundle;
import android.view.KeyEvent;

public class GameHelpActivity extends GameBaseActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_help);
		initAdview();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			activityExitAnim();
		}
		return super.onKeyDown(keyCode, event);
	}

}
