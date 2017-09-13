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
 * ����������
 * 
 * @author wyf
 * 
 */
public class SoundPlayer {

	private static MediaPlayer music;
	private static SoundPool soundPool;

	private static boolean musicSt = true; // ���ֿ���
	private static boolean soundSt = true; // ��Ч����
	private static Context context;

	private static final int[] musicId = { R.raw.start };
	private static Map<Integer, Integer> soundMap; // ��Ч��Դid����ع������Դid��ӳ���ϵ��

	/**
	 * ��ʼ������
	 * 
	 * @param c
	 */
	public static void init(Context c) {
		context = c;
		initMusic();
		initSound();
	}

	// ��ʼ����Ч������
	private static void initSound() {
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);
		soundMap = new HashMap<Integer, Integer>();
		soundMap.put(R.raw.effect, soundPool.load(context, R.raw.effect, 0));
	}

	// ��ʼ�����ֲ�����
	private static void initMusic() {
		music = MediaPlayer.create(context, musicId[0]);
		music.setLooping(true);
	}

	/**
	 * ������Ч
	 * 
	 * @param resId
	 *            ��Ч��Դid
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
	 * ��ͣ����
	 */
	public static void pauseMusic() {
		if (music!=null&&music.isPlaying())
			music.pause();
	}

	/**
	 * ֹͣ����
	 */
	public static void stopMusic() {
		if (music!=null&&music.isPlaying())
			music.stop();
	}

	/**
	 * ��������
	 */
	public static void startMusic() {
		if (music!=null&&musicSt)
			music.start();
	}

	/**
	 * ������ֿ���״̬
	 * 
	 * @return
	 */
	public static boolean isMusicSt() {
		return musicSt;
	}

	/**
	 * �������ֿ���
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
	 * �����Ч����״̬
	 * 
	 * @return
	 */
	public static boolean isSoundSt() {
		return soundSt;
	}

	/**
	 * ������Ч����
	 * 
	 * @param soundSt
	 */
	public static void setSoundSt(boolean soundSt) {
		SoundPlayer.soundSt = soundSt;
	}

	/**
	 * �������������
	 */
	public static void boom() {
		playSound(R.raw.effect);
	}
}
