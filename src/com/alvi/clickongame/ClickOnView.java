package com.alvi.clickongame;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClickOnView extends View{
	private static final String High_Score="High_Score";
	private SharedPreferences preferences;
	private int spotsTouched;
	private int score;
	private int level;
	private int viewWidth;
	private int viewHeiight;
	private long animationTime;
	private boolean gameover;
	private boolean gamepaused;
	private boolean dialogDisplayed;
	private int highScore;
	private final Queue<ImageView> spots=new ConcurrentLinkedQueue<ImageView>();
	private final Queue<Animator> animators=new ConcurrentLinkedQueue<Animator>();
	private TextView highScoreText;
	private TextView levelText;
	private TextView scoreText;
	private RelativeLayout relativeLayout;
	private Resources resources;
	private LayoutInflater layoutInflater;
	private LinearLayout livesLayout;
	
	
	private static final int Initial_animation_speed=6000;
	private static final Random random=new Random();
	private static final int Spot_diameter=100;
	private static final float Scale_X=0.23f;
	private static final float Scale_Y=0.25f;
	private static final int Initial_Spots=5;
	private static final int Spot_delay=500;
	private static final int Lives=3;
	private static final int Max_lives=7;
	private static final int New_Level=10;
	private Handler spotHandler;
	private static final int Hit_Sound_Id=1;
	private static final int Miss_Sound_Id=2;
	private static final int DisAppear_Sound_id=3;
	private static final int Sound_Priority=1;
	private static final int Sound_Quality=100;
	private static final int Max_Streams=4;
	private SoundPool soundPool;
	private int volume;
	private Map<Integer,Integer> soundMap;

	
	public ClickOnView(Context context,SharedPreferences sharedPreferences,RelativeLayout layout){
		super(context);
		preferences=sharedPreferences;
		highScore=preferences.getInt(High_Score, 0);
		
		resources=context.getResources();
		layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		relativeLayout=layout;
		
		livesLayout=(LinearLayout)relativeLayout.findViewById(R.id.lifeLinearLayout);
		highScoreText=(TextView)relativeLayout.findViewById(R.id.highScoretextView);
		scoreText=(TextView)relativeLayout.findViewById(R.id.scoretextView);
		levelText=(TextView)relativeLayout.findViewById(R.id.leveltextView);
		spotHandler=new Handler();
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		viewHeiight=h;
		viewWidth=w;
	}
	public void Puase(){
		gamepaused=true;
		soundPool.release();
		soundPool=null;
		cancelAnimations();
	}
	private void cancelAnimations(){
		for(Animator animator:animators){
			animator.cancel();
		}
		for(ImageView view :spots){
			relativeLayout.removeView(view);
			
		}
		spotHandler.removeCallbacks(addSpotRunnable);
		animators.clear();
		spots.clear();
	}
	
	public void resume(Context context){
		gamepaused=false;
		initialSoundEffects(context);
		if(!dialogDisplayed){
			resetGame();
		}
	}
	public void resetGame(){
		spots.clear();
		animators.clear();
		livesLayout.removeAllViews();
		
		animationTime=Initial_animation_speed;
		spotsTouched=0;
		score=0;
		level=1;
		gameover=false;
		displayScores();
		for(int i=0;i<Lives;i++){
			livesLayout.addView((ImageView)layoutInflater.inflate(R.layout.life, null));
		}
		for(int i=1;i<=Initial_Spots;++i){
			spotHandler.postDelayed(addSpotRunnable, i*Spot_delay);	
		}
		
	}
	private void initialSoundEffects(Context context){
		soundPool=new SoundPool(Max_Streams,AudioManager.STREAM_MUSIC,Sound_Quality);
		AudioManager manager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		volume=manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		soundMap=new HashMap<Integer,Integer>();
		soundMap.put(Hit_Sound_Id,soundPool.load(context, R.raw.target_hit,Sound_Priority));
		soundMap.put(Hit_Sound_Id,soundPool.load(context, R.raw.blocker_hit,Sound_Priority));
		soundMap.put(Hit_Sound_Id,soundPool.load(context, R.raw.cannon_fire,Sound_Priority));
		
	}
	private void displayScores(){
		highScoreText.setText("High Score : "+highScore);
		scoreText.setText("Score : "+score);
		levelText.setText("Level : "+level);
	}
	private Runnable addSpotRunnable=new Runnable()
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			addnewImage();
			
		}
		
	};
	@SuppressLint("NewApi") public void addnewImage(){
		int x=random.nextInt(viewWidth-Spot_diameter);
		int y=random.nextInt(viewHeiight-Spot_diameter);
		int x2=random.nextInt(viewWidth-Spot_diameter);
		int y2=random.nextInt(viewHeiight-Spot_diameter);
		
		final ImageView spot=(ImageView)layoutInflater.inflate(R.layout.untouched, null);
		spots.add(spot);
		spot.setLayoutParams(new RelativeLayout.LayoutParams(Spot_diameter,Spot_diameter));
		spot.setImageResource(random.nextInt(2)==0?R.drawable.target:R.drawable.compass);
		spot.setX(x);
		spot.setY(y);
		spot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				touchedSpot(spot);
				
			}
		});
		relativeLayout.addView(spot);
		spot.animate().x(x2).y(y2).scaleX(Scale_X).scaleY(Scale_Y).setDuration(animationTime).setListener(
				new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						animators.add(animation);
					}
					public void onAnimationEnd(Animator animation){
						animators.remove(animation);
						if(!gamepaused && spots.contains(spot)){
							missedSpot(spot);
						}
					}
					
					
				}
				);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		if(soundPool!=null){
			soundPool.play(Miss_Sound_Id, volume, volume, Sound_Priority, 0, 1f);
			
		}
		score-=15*level;
		score=Math.max(score, 0);
		displayScores();
		return true;
	}
	private void touchedSpot(ImageView spot){
		relativeLayout.removeView(spot);
		spots.remove();
		++spotsTouched;
		score+=10*level;
		if(soundPool!=null){
			soundPool.play(Hit_Sound_Id, volume, volume, Sound_Priority, 0,1f);
			
		}
		if(spotsTouched%10==0){
			++level;
			animationTime*=0.95;
			if(livesLayout.getChildCount()<Max_lives){
				ImageView life=(ImageView)layoutInflater.inflate(R.layout.life, null);
				livesLayout.addView(life);
			}
		}
		displayScores();
		if(!gameover){
			addnewImage();
		}
	}
	private void missedSpot(ImageView spot){
		spots.remove(spot);
		relativeLayout.removeView(spot);
		if(gameover)
			return;
		if(soundPool!=null)
			soundPool.play(DisAppear_Sound_id, volume, volume, Sound_Priority, 0, 1f);
		
		if(livesLayout.getChildCount()==0){
			gameover=true;
			if(score>highScore){
				SharedPreferences.Editor editor=preferences.edit();
				editor.putInt(High_Score, score);
				editor.commit();
				highScore=score;
				
			}
			cancelAnimations();
			Builder dialogBuilder=new AlertDialog.Builder(getContext());
			dialogBuilder.setTitle("Game Over");
			dialogBuilder.setMessage("Game Over\nScore : "+score);
			dialogBuilder.setPositiveButton("Reset",
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							displayScores();
							dialogDisplayed=false;
							resetGame();
						}
					}
					);
			dialogDisplayed=true;
			dialogBuilder.show();
					
		}
		else{
			livesLayout.removeViewAt(livesLayout.getChildCount()-1);
			addnewImage();
			
		}
		
		
	}
	

}
