//五子棋的View

package com.fairy.wuziqi.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.fairy.wuziqi.R;
import com.fairy.wuziqi.uilts.ChessFormUtil;
import com.fairy.wuziqi.uilts.KeyCode;
import com.fairy.wuziqi.uilts.SoundPlayer;

/*棋盘一共12×12格  和梦幻的一样哦
 * 棋盘居中
 *
 */
/**
 * 简单的类，表单当前落棋点及其分数
 */
class ChessPoint {
	int x;
	int y;
	int score;
}

// public class GobangView extends View implements Runnable {
public class GameView extends View {

	

	private final int CHESS_GRID = KeyCode.GRID_SIZE - 1;

	private final static int CHECK_DIR = 4; // 当前位置的四个方向，横、竖、左斜、右斜

	private int chess_dia = 22; // 棋的直径

	private int grid_width; // 棋盘格的宽度
	private int mStartX;// 棋盘定位的左上角X
	private int mStartY;// 棋盘定位的左上角Y

	private Bitmap[] mChessBW; // 黑棋和白棋

	private int[][] mChessTable = new int[CHESS_GRID][CHESS_GRID]; // 网格

	private int[][][] computerTable = new int[CHESS_GRID][CHESS_GRID][CHECK_DIR]; // 电脑棋形表
	private int[][][] playerTable = new int[CHESS_GRID][CHESS_GRID][CHECK_DIR]; // 电脑棋形表

	public static final int BLACK = 2; // 电脑执黑棋
	public static final int WHITE = 1; // 玩家执白棋

	private int whoTurn = WHITE; // 该谁下了，该下白棋了=2，该下黑棋了=1.
	// 这里先下黑棋（黑棋以后设置为机器自动下的棋子）
	private int mWinFlag = 0;

	private static final int GAMESTATE_PRE = 0;
	public static final int GAMESTATE_RUN = 1;
	public static final int GAMESTATE_PAUSE = 2;
	private static final int GAMESTATE_END = 3;
	public int mGameState = GAMESTATE_RUN; // 游戏阶段：0=尚未游戏，1=正在进行游戏，2=游戏结束
	public TextView mStatusTextView; // 根据游戏状态设置显示的文字
	private CharSequence mText;
	private CharSequence STRING_WIN = "白棋胜利了! \n点击屏幕开始新游戏!";
	private CharSequence STRING_LOSE = "黑棋胜利了! \n点击屏幕开始新游戏!";
	private CharSequence STRING_EQUAL = "酷！你们是平手! \n点击屏幕开始新游戏!";
	private Paint mPaint;
	private boolean bitmapLoaded = false;
	private ChessFormUtil cfUtil;

	// 保存前5个较好的落子点
	private ChessPoint[] fiveBetterPoints = new ChessPoint[5];

	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		cfUtil = new ChessFormUtil();
		mPaint =new Paint();
		mPaint.setAntiAlias(true);
	}

	public GameView(Context context, AttributeSet attrs) { // 好像调用的是这个构造函数，为什么不是前面的呢
		super(context, attrs);
		this.setFocusable(true); // 20090530
		this.setFocusableInTouchMode(true);
		cfUtil = new ChessFormUtil();

	}

	// 这里画棋子后来没有用图片画，而是直接画了圆。因为我做的图片不好看。
	// 初始化黑白棋的Bitmap
	public void init() {
		mGameState = 1; // 设置游戏为开始状态
		whoTurn = WHITE; // 初始为先下黑棋
		mWinFlag = 0; // 清空输赢标志。
		// 清空各种表格
		for (int i = 0; i < CHESS_GRID; i++) {
			for (int j = 0; j < CHESS_GRID; j++) {
				mChessTable[i][j] = 0;
				for (int k = 0; k < CHECK_DIR; k++) {
					computerTable[i][j][k] = 0;
					playerTable[i][j][k] = 0;
				}
			}

		}
	}
	
	
	public void setTextView(TextView tv) {
		mStatusTextView = tv;
		mStatusTextView.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w <= h)
			grid_width = w / KeyCode.GRID_SIZE;
		else
			grid_width = h / KeyCode.GRID_SIZE;

		chess_dia = grid_width - 2;
		mStartX = (w - KeyCode.GRID_SIZE * grid_width) >> 1;
		mStartY = (h - KeyCode.GRID_SIZE * grid_width) >> 1;

		if (!bitmapLoaded) {
			mChessBW = new Bitmap[2];

			Bitmap bitmap = Bitmap.createBitmap(chess_dia, chess_dia,
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			Resources r = this.getContext().getResources();

			Drawable tile = r.getDrawable(R.drawable.black);
			tile.setBounds(0, 0, chess_dia, chess_dia);
			tile.draw(canvas);
			mChessBW[0] = bitmap;

			// bitmap.recycle(); //
			// 清除该bitmap的内存，保证该Bitmap被回收，但是一般不需要手动调用，正常的GC会清除该Bitmap

			bitmap = Bitmap.createBitmap(chess_dia, chess_dia,
					Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap); // Bitmap + Canvas + Drawable ==
			// 标准的Bitmap绘制。
			tile = r.getDrawable(R.drawable.whrite);
			tile.setBounds(0, 0, chess_dia, chess_dia);
			tile.draw(canvas);
			mChessBW[1] = bitmap;
			bitmapLoaded = true;
			init();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (mGameState) {
			case GAMESTATE_PRE:
				break;
			case GAMESTATE_RUN: {
				int x;
				int y;
				float x0 = grid_width - (event.getX() - mStartX) % grid_width;
				float y0 = grid_width - (event.getY() - mStartY) % grid_width;
				if (x0 < (grid_width >> 1)) {
					x = (int) ((event.getX() - mStartX) / grid_width);
				} else {
					x = (int) ((event.getX() - mStartX) / grid_width) - 1;
				}
				if (y0 < (grid_width >> 1)) {
					y = (int) ((event.getY() - mStartY) / grid_width);
				} else {
					y = (int) ((event.getY() - mStartY) / grid_width) - 1;
				}
				Log.v("x,y", "" + y + "," + x);
				if ((x >= 0 && x < CHESS_GRID) && (y >= 0 && y < CHESS_GRID)) {
					if (mChessTable[x][y] == 0) {
						if (whoTurn == WHITE) {
							putChess(x, y, WHITE);
							SoundPlayer.playSound(R.raw.effect);
							if (checkWin(WHITE)) {
								mText = STRING_WIN;
								mGameState = GAMESTATE_END;
								showTextView(mText);
							} else if (checkFull()) {// 如果棋盘满了
								mText = STRING_EQUAL;
								mGameState = GAMESTATE_END;
								showTextView(mText);
							}
							
							whoTurn = BLACK;
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// 电脑开始分析
							analyzeChess();

							// 黑棋下了之后开始分析黑棋此时是否胜利。
							if (checkWin(BLACK)) { // 如果是黑棋赢了
								mText = STRING_LOSE;
								mGameState = GAMESTATE_END;
								showTextView(mText);
							} else if (checkFull()) {// 如果棋盘满了
								mText = STRING_EQUAL;
								mGameState = GAMESTATE_END;
								showTextView(mText);
							}

							whoTurn = WHITE;
						}
					}
				}
			}

				break;
			case GAMESTATE_PAUSE:
				break;
			case GAMESTATE_END:
				mGameState = GAMESTATE_RUN;
				this.setVisibility(View.VISIBLE);
				this.mStatusTextView.setVisibility(View.INVISIBLE);
				this.init();
				this.invalidate();
				break;
			}

			this.invalidate();
		}
		return true;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		Log.e("lee", " " + keyCode);

		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			switch (mGameState) {
			case GAMESTATE_PRE:
				break;
			case GAMESTATE_RUN:
				break;
			case GAMESTATE_PAUSE:
				break;
			case GAMESTATE_END: {// 游戏结束后，按CENTER键继续
				Log.e("lee", "FIRE");
				mGameState = GAMESTATE_RUN;
				this.setVisibility(View.VISIBLE);
				this.mStatusTextView.setVisibility(View.INVISIBLE);
				this.init();
				this.invalidate();

			}
				break;
			}
		}

		return super.onKeyDown(keyCode, msg);
	}

	private void clearArray(int[] array) {
		for (int i = 0; i < array.length; i++)
			array[i] = 0;
	}

	private void clearChessArray() {
		int i, j;
		for (i = 0; i < CHESS_GRID; i++) {
			for (j = 0; j < CHESS_GRID; j++) {
				computerTable[i][j][0] = 0;
				computerTable[i][j][1] = 0;
				computerTable[i][j][2] = 0;
				computerTable[i][j][3] = 0;

				playerTable[i][j][0] = 0;
				playerTable[i][j][1] = 0;
				playerTable[i][j][2] = 0;
				playerTable[i][j][3] = 0;

			}
		}

		for (i = 0; i < 5; i++) {
			fiveBetterPoints[i] = null;
		}

	}

	/**
	 * 分析具体的方向
	 * 
	 * @param tmpChess
	 *            该方向的棋型
	 */
	private int analyzeDir(int[] tmpChess, int isWho) {
		// 如果满足活四，就不活三、满足活三就不冲四，如此...
		int score = 0;
		if (cfUtil.analyzeWulian(tmpChess, isWho)) {
			score = ChessFormUtil.WU_LIAN;
		} else if (cfUtil.analyzeHuosi(tmpChess, isWho)) {
			score = ChessFormUtil.HUO_SI;
		} else if (cfUtil.analyzeHuosan(tmpChess, isWho)) {
			score = ChessFormUtil.HUO_SAN;
		} else if (cfUtil.analyzeChongsi(tmpChess, isWho)) {
			score = ChessFormUtil.CHONG_SI;
		} else if (cfUtil.analyzeHuoEr(tmpChess, isWho)) {
			score = ChessFormUtil.HUO_ER;
		} else if (cfUtil.analyzeMianSan(tmpChess, isWho)) {
			score = ChessFormUtil.MIAN_SAN;
		} else if (cfUtil.analyzeMianEr(tmpChess, isWho)) {
			score = ChessFormUtil.MIAN_ER;
		} else {
			score = 0;
		}

		return score;
	}

	/**
	 * 电脑智能分析最好的下棋地点
	 */
	private void analyzeChess() {
		if (whoTurn == BLACK) {
			// 与当前棋子相关需要分析棋型的棋子数组
			clearChessArray();
			analyzeChessMater(computerTable, BLACK, 0, 0, CHESS_GRID,
					CHESS_GRID);
			// 分析玩家的棋型/////////////////////////////////////////////////////
			analyzeChessMater(playerTable, WHITE, 0, 0, CHESS_GRID, CHESS_GRID);

			ChessPoint bestPoint = findBestPoint();
			// Log.v("Best Chess", "x = " + bestPoint.x + ", y = " +
			// bestPoint.y);
			if(bestPoint!=null){
			mChessTable[bestPoint.y][bestPoint.x] = BLACK;
			SoundPlayer.playSound(R.raw.effect);
			}

		}
	}

	/**
	 * 分析指定的棋型
	 * 
	 * @param materChess
	 *            棋型数组
	 */
	private void analyzeChessMater(int[][][] materChess, int isWho, int sx,
			int sy, int ex, int ey) {
		int[] tmpChess = new int[ChessFormUtil.ANALYZE_LEN];

		// 具体代码...
		int i, j, k;
		// 分析电脑的棋型/////////////////////////////////////////////////////
		for (i = sy; i < ey; i++) {
			for (j = sx; j < ex; j++) {
				if (mChessTable[i][j] == 0) {
					// 找出横向的棋子的棋型
					clearArray(tmpChess);
					for (k = 1; k <= ChessFormUtil.HALF_LEN; k++) {
						if ((j + k) < ex) {
							tmpChess[ChessFormUtil.HALF_LEN + (k - 1)] = mChessTable[i][j
									+ k];
						}
						if ((j - k) >= 0) {
							tmpChess[ChessFormUtil.HALF_LEN - k] = mChessTable[i][j
									- k];
						}
					}
					materChess[i][j][0] = analyzeDir(tmpChess, isWho);
					// 找出竖向的棋子的棋型
					clearArray(tmpChess);

					for (k = 1; k <= ChessFormUtil.HALF_LEN; k++) {
						if ((i + k) < ey) {
							tmpChess[ChessFormUtil.HALF_LEN + (k - 1)] = mChessTable[i
									+ k][j];
						}
						if ((i - k) >= 0) {
							tmpChess[ChessFormUtil.HALF_LEN - k] = mChessTable[i
									- k][j];
						}
					}
					materChess[i][j][1] = analyzeDir(tmpChess, isWho);
					// 找出左斜的棋子的棋型
					clearArray(tmpChess);
					for (k = 1; k <= ChessFormUtil.HALF_LEN; k++) {
						if ((i + k) < ey && (j + k) < ex) {
							tmpChess[ChessFormUtil.HALF_LEN + (k - 1)] = mChessTable[i
									+ k][j + k];
						}
						if ((i - k) >= 0 && (j - k) >= 0) {
							tmpChess[ChessFormUtil.HALF_LEN - k] = mChessTable[i
									- k][j - k];
						}
					}
					materChess[i][j][2] = analyzeDir(tmpChess, isWho);
					// 找出右斜的棋子的棋型
					clearArray(tmpChess);
					for (k = 1; k <= ChessFormUtil.HALF_LEN; k++) {
						if ((i - k) >= 0 && (j + k) < ex) {
							tmpChess[ChessFormUtil.HALF_LEN + (k - 1)] = mChessTable[i
									- k][j + k];
						}
						if ((i + k) < ey && (j - k) >= 0) {
							tmpChess[ChessFormUtil.HALF_LEN - k] = mChessTable[i
									+ k][j - k];
						}
					}
					materChess[i][j][3] = analyzeDir(tmpChess, isWho);
					// mChessTable[i][]
					// 分析最大分数，保存该点坐标...
				}
			}
		}
	}

	private void insertBetterChessPoint(ChessPoint cp) {
		int i, j = 0;
		ChessPoint tmpcp = null;
		for (i = 0; i < 5; i++) {
			if (null != fiveBetterPoints[i]) {
				if (cp.score > fiveBetterPoints[i].score) {
					tmpcp = fiveBetterPoints[i];
					fiveBetterPoints[i] = cp;
					for (j = i; j < 5; j++) {
						if (null != fiveBetterPoints[j]) {
							if (tmpcp.score > fiveBetterPoints[j].score) {
								tmpcp = fiveBetterPoints[j];
								fiveBetterPoints[j] = tmpcp;
							}
						} else {
							fiveBetterPoints[j] = tmpcp;
							break;
						}
					}
					break;
				}
			} else {
				fiveBetterPoints[i] = cp;
				break;
			}
		}

		tmpcp = null;
	}

	/**
	 * 找到最佳点
	 * 
	 * @return 最佳点
	 */
	private ChessPoint findBestPoint() {
		int i, j;
		ChessPoint point;
		int maxScore = 0;
		int tmpScore = 0;
		for (i = 0; i < CHESS_GRID; i++) {
			for (j = 0; j < CHESS_GRID; j++) {
				// 电脑比较
				tmpScore = computerTable[i][j][0];
				tmpScore += computerTable[i][j][1];
				tmpScore += computerTable[i][j][2];
				tmpScore += computerTable[i][j][3];
				if (maxScore <= tmpScore) {
					maxScore = tmpScore;
					point = new ChessPoint();
					point.x = j;
					point.y = i;
					point.score = maxScore;
					// 不能容忍0作为最小分数插入，因为
					// 哪怕该点有棋，它的分数也是为0
					// 不过这里不做判断就会将有棋的地方替换掉
					if (maxScore > 0) {
						insertBetterChessPoint(point);
					}

				}
				// 玩家比较
				tmpScore = playerTable[i][j][0];
				tmpScore += playerTable[i][j][1];
				tmpScore += playerTable[i][j][2];
				tmpScore += playerTable[i][j][3];
				if (maxScore <= tmpScore) {
					maxScore = tmpScore;
					point = new ChessPoint();
					point.x = j;
					point.y = i;
					point.score = maxScore;
					if (maxScore > 0) {
						insertBetterChessPoint(point);
					}
				}

			}
		}

		// Log.v("cmaxpoint = ", "" + cMaxScore);
		// Log.v("pmaxpoint = ", "" + pMaxScore);

		return analyzeBetterChess();
	}

	/**
	 * 找到几点中比较好的点
	 * 
	 * @return 最佳点
	 */
	private ChessPoint findBetterPoint(int sx, int sy, int ex, int ey) {
		int i, j;
		ChessPoint point;
		int maxScore = 0;
		int tmpScore = 0;
		for (i = sy; i < ey; i++) {
			for (j = sx; j < ex; j++) {
				// 电脑比较
				tmpScore = computerTable[i][j][0];
				tmpScore += computerTable[i][j][1];
				tmpScore += computerTable[i][j][2];
				tmpScore += computerTable[i][j][3];
				if (maxScore <= tmpScore) {
					maxScore = tmpScore;
					point = new ChessPoint();
					point.x = j;
					point.y = i;
					point.score = maxScore;
					insertBetterChessPoint(point);
				}
				// 玩家比较
				tmpScore = playerTable[i][j][0];
				tmpScore += playerTable[i][j][1];
				tmpScore += playerTable[i][j][2];
				tmpScore += playerTable[i][j][3];
				if (maxScore <= tmpScore) {
					maxScore = tmpScore;
					point = new ChessPoint();
					point.x = j;
					point.y = i;
					point.score = maxScore;
					insertBetterChessPoint(point);
				}

			}
		}

		return fiveBetterPoints[0];
	}

	private ChessPoint analyzeBetterChess() {
		if (fiveBetterPoints[0]!=null&&fiveBetterPoints[0].score > 30) {
			return fiveBetterPoints[0];
		} else {
			ChessPoint betterPoint = null;
			ChessPoint tmpPoint = null;

			int goodIdx = 0;
			int i = 0;
			int startx, starty, endx, endy;
			ChessPoint[] fbpTmp = new ChessPoint[5];
			for (i = 0; i < 5; i++) {
				fbpTmp[i] = fiveBetterPoints[i];
			}

			for (i = 0; i < 5; i++) {
				if (fbpTmp[i] == null)
					break;
				mChessTable[fbpTmp[i].y][fbpTmp[i].x] = BLACK;
				clearChessArray();

				startx = fbpTmp[i].x - 5;
				starty = fbpTmp[i].y - 5;

				if (startx < 0) {
					startx = 0;
				}

				if (starty < 0) {
					starty = 0;
				}

				endx = startx + 10;
				endy = starty + 10;

				if (endx > CHESS_GRID) {
					endx = CHESS_GRID;
				}

				if (endy > CHESS_GRID) {
					endy = CHESS_GRID;
				}
				analyzeChessMater(computerTable, BLACK, startx, starty, endx,
						endy);
				// 分析玩家的棋型/////////////////////////////////////////////////////
				analyzeChessMater(playerTable, WHITE, startx, starty, endx,
						endy);
				tmpPoint = findBetterPoint(startx, starty, endx, endy);
				if (betterPoint != null) {
					if (betterPoint.score <= tmpPoint.score) {
						betterPoint = tmpPoint;
						goodIdx = i;
					}
				} else {
					betterPoint = tmpPoint;
					goodIdx = i;
				}

				mChessTable[fbpTmp[i].y][fbpTmp[i].x] = 0;
			}
			tmpPoint = null;
			betterPoint = null;
			return fbpTmp[goodIdx];
		}

	}

	@Override
	public void onDraw(Canvas canvas) {
		{
			Paint paintRect = new Paint();
			paintRect.setColor(Color.rgb(98,98, 98));
			paintRect.setStrokeWidth(2);
			paintRect.setStyle(Style.STROKE);
			paintRect.setAntiAlias(true);
			for (int i = 0; i < KeyCode.GRID_SIZE; i++) {
				for (int j = 0; j < KeyCode.GRID_SIZE; j++) {
					int mLeft = i * grid_width + mStartX;
					int mTop = j * grid_width + mStartY;
					int mRright = mLeft + grid_width;
					int mBottom = mTop + grid_width;
					canvas.drawRect(mLeft, mTop, mRright, mBottom, paintRect);
				}
			}

			// 画棋盘的外边框
			paintRect.setStrokeWidth(4);
			canvas.drawRect(mStartX, mStartY, mStartX + grid_width * KeyCode.GRID_SIZE,
					mStartY + grid_width * KeyCode.GRID_SIZE, paintRect);
		}

		// 画棋子

		if (bitmapLoaded) {
			for (int i = 0; i < CHESS_GRID; i++) {
				for (int j = 0; j < CHESS_GRID; j++) {
					if (mChessTable[i][j] == BLACK) {
						// 通过图片来画
						canvas.drawBitmap(mChessBW[0], mStartX + (i + 1)
								* grid_width - (chess_dia >> 1), mStartY
								+ (j + 1) * grid_width - (chess_dia >> 1),
								mPaint);
						///SoundPlayer.playSound(R.raw.effect);
					} else if (mChessTable[i][j] == WHITE) {
						// 通过图片来画
						canvas.drawBitmap(mChessBW[1], mStartX + (i + 1)
								* grid_width - (chess_dia >> 1), mStartY
								+ (j + 1) * grid_width - (chess_dia >> 1),
								mPaint);
						//SoundPlayer.playSound(R.raw.effect);

					}

				}
			}
		}
	}

	private void putChess(int x, int y, int blackwhite) {
		mChessTable[x][y] = blackwhite;
	}

	private boolean checkWin(int wbflag) {
		for (int i = 0; i < KeyCode.GRID_SIZE - 1; i++)
			for (int j = 0; j < KeyCode.GRID_SIZE - 1; j++) {
				// 就是检查四条线，参见英国国旗
				// 检测横轴五个相连
				if (((i + 4) < (KeyCode.GRID_SIZE - 1))
						&& (mChessTable[i][j] == wbflag)
						&& (mChessTable[i + 1][j] == wbflag)
						&& (mChessTable[i + 2][j] == wbflag)
						&& (mChessTable[i + 3][j] == wbflag)
						&& (mChessTable[i + 4][j] == wbflag)) {
					// Log.e("check win or loss:", wbflag + "win");

					mWinFlag = wbflag;
				}

				// 纵轴5个相连
				if (((j + 4) < (KeyCode.GRID_SIZE - 1))
						&& (mChessTable[i][j] == wbflag)
						&& (mChessTable[i][j + 1] == wbflag)
						&& (mChessTable[i][j + 2] == wbflag)
						&& (mChessTable[i][j + 3] == wbflag)
						&& (mChessTable[i][j + 4] == wbflag)) {
					// Log.e("check win or loss:", wbflag + "win");

					mWinFlag = wbflag;
				}

				// 左上到右下5个相连
				if (((j + 4) < (KeyCode.GRID_SIZE - 1)) && ((i + 4) < (KeyCode.GRID_SIZE - 1))
						&& (mChessTable[i][j] == wbflag)
						&& (mChessTable[i + 1][j + 1] == wbflag)
						&& (mChessTable[i + 2][j + 2] == wbflag)
						&& (mChessTable[i + 3][j + 3] == wbflag)
						&& (mChessTable[i + 4][j + 4] == wbflag)) {
					// Log.e("check win or loss:", wbflag + "win");

					mWinFlag = wbflag;
				}

				// 右上到左下5个相连
				if (((i - 4) >= 0) && ((j + 4) < (KeyCode.GRID_SIZE - 1))
						&& (mChessTable[i][j] == wbflag)
						&& (mChessTable[i - 1][j + 1] == wbflag)
						&& (mChessTable[i - 2][j + 2] == wbflag)
						&& (mChessTable[i - 3][j + 3] == wbflag)
						&& (mChessTable[i - 4][j + 4] == wbflag)) {
					// Log.e("check win or loss:", wbflag + "win");

					mWinFlag = wbflag;
				}
			}

		if (mWinFlag == wbflag) {
			return true;
		} else
			return false;

	}

	private boolean checkFull() {
		int mNotEmpty = 0;
		for (int i = 0; i < KeyCode.GRID_SIZE - 1; i++)
			for (int j = 0; j < KeyCode.GRID_SIZE - 1; j++) {
				if (mChessTable[i][j] != 0)
					mNotEmpty += 1;
			}

		if (mNotEmpty == (KeyCode.GRID_SIZE - 1) * (KeyCode.GRID_SIZE - 1))
			return true;
		else
			return false;
	}

	private void showTextView(CharSequence mT) {
		this.mStatusTextView.setText(mT);
		mStatusTextView.setVisibility(View.VISIBLE);

	}

}