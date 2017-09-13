package com.fairy.wuziqi.uilts;

import java.util.HashMap;
import java.util.Map;

import com.fairy.wuziqi.R;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;


/**
 * 声音控制类
 * 
 * @author wyf
 * 
 */
public class SoundPlayer {

	private static MediaPlayer music;
	private static SoundPool soundPool;

	private static boolean musicSt = true; // 音乐开关
	private static boolean soundSt = true; // 音效开关
	private static Context context;

	private static final int[] musicId = { R.raw.start };
	private static Map<Integer, Integer> soundMap; // 音效资源id与加载过后的音源id的映射关系表

	/**
	 * 初始化方法
	 * 
	 * @param c
	 */
	public static void init(Context c) {
		context = c;
		initMusic();
		initSound();
	}

	// 初始化音效播放器
	private static void initSound() {
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);
		soundMap = new HashMap<Integer, Integer>();
		soundMap.put(R.raw.effect, soundPool.load(context, R.raw.effect, 0));
	}

	// 初始化音乐播放器
	private static void initMusic() {
		music = MediaPlayer.create(context, musicId[0]);
		music.setLooping(true);
	}

	/**
	 * 播放音效
	 * 
	 * @param resId
	 *            音效资源id
	 */
	public static void playSound(int resId) {
		if (soundSt == false)
			return;

		Integer soundId = soundMap.get(resId);
		if (soundId != null)
			Log.d("lee", "lee------->paly");
			soundPool.play(soundId, 1, 1, 1, 0, 1);
	}

	/**
	 * 暂停音乐
	 */
	public static void pauseMusic() {
		if (music!=null&&music.isPlaying())
			music.pause();
	}

	/**
	 * 停止音乐
	 */
	public static void stopMusic() {
		if (music!=null&&music.isPlaying())
			music.stop();
	}

	/**
	 * 播放音乐
	 */
	public static void startMusic() {
		if (music!=null&&musicSt)
			music.start();
	}

	/**
	 * 获得音乐开关状态
	 * 
	 * @return
	 */
	public static boolean isMusicSt() {
		return musicSt;
	}

	/**
	 * 设置音乐开关
	 * 
	 * @param musicSt
	 */
	public static void setMusicSt(boolean musicSt) {
		SoundPlayer.musicSt = musicSt;
		if(music!=null){
		if (musicSt){
			music.start();
		}
		else{
			music.pause();
		}
		}
	}

	/**
	 * 获得音效开关状态
	 * 
	 * @return
	 */
	public static boolean isSoundSt() {
		return soundSt;
	}

	/**
	 * 设置音效开关
	 * 
	 * @param soundSt
	 */
	public static void setSoundSt(boolean soundSt) {
		SoundPlayer.soundSt = soundSt;
	}

	/**
	 * 发出‘邦’的声音
	 */
	public static void boom() {
		playSound(R.raw.effect);
	}
}
