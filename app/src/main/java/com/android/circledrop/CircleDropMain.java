package com.android.circledrop;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CircleDropMain extends AppCompatActivity {

    private final static String LOG_TAG = "CircleDropMain";
    private final static int LIVES = 3;
    private final static int STARTING_SCORE = 0;
    private final static String START_STATE = "startstate";
    private final static String PAUSE_STATE = "pausestate";
    private final static String END_STATE = "endstate";
    private final static String NEW_STATE = "newstate";
    private final static int PLAYER_W = 75;
    private final static int PLAYER_H = 75;

    private RelativeLayout mGameView;
    private Button mLeftButton;
    private Button mRightButton;
    private TextView mScoreView;
    private TextView mLivesView;
    private LinearLayout mPlayerView;

    private int mCurrentScore;
    private int mCurrentLives;
    private boolean mStartState;
    private boolean mPauseState;
    private boolean mEndState;
    private boolean mNewState;
    private boolean mIsInMotion;
    private int mScreenWidth;
    private int mScreenHeight;
    private float mPlayerX;
    private float mPlayerY;
    private float eventX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGameView = (RelativeLayout) findViewById(R.id.gameView);
        mLeftButton = (Button) findViewById(R.id.leftButton);
        mRightButton = (Button) findViewById(R.id.rightButton);
        mScoreView = (TextView) findViewById(R.id.scoreView);
        mLivesView = (TextView) findViewById(R.id.livesView);

        getScreenSize();
        mPlayerX = (mScreenWidth/2) - (PLAYER_W/2);
        mPlayerY = 50;    //1250

        changeStateTo(NEW_STATE);
        newGame();
        updateCommandBar();

        // when Pause is pressed, game is paused and text changes to Resume
        // when Resume is pressed, game is resumed and text changes to Pause
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewState) {
                    // pause the game, but don't put it into Pause state; might need to have
                    // another state for when the game is paused while in New State
                }
                //set the game to pause and text changes to Resume
                else if (mStartState) {
                    changeStateTo(PAUSE_STATE);
                    pauseGame();
                    updateCommandBar();
                } else if (mPauseState) {
                    changeStateTo(START_STATE);
                    startGame();
                    updateCommandBar();
                } else if (mEndState) {
                    // no need, button should be disabled for this state
                }
            }
        });

        // when End is pressed, game ends and text changes to New
        // when New is pressed, score sets to zero, lives to 3, left button is Pause, and text
        // changes to Start
        // at this point, player can place white circles on screen by touching screen, the longer
        // the touch, the larger the circle
        // when Start is pressed, white circles start falling, text changes to End
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewState) {
                    changeStateTo(START_STATE);
                    startGame();
                    updateCommandBar();
                } else if (mStartState) {
                    changeStateTo(END_STATE);
                    endGame();
                    updateCommandBar();
                } else if (mPauseState) {
                    changeStateTo(END_STATE);
                    endGame();
                    updateCommandBar();
                } else if (mEndState) {
                    changeStateTo(NEW_STATE);
                    newGame();
                    updateCommandBar();
                }

            }
        });

    }


    private void changeStateTo(String state) {
        switch (state) {
            case START_STATE:
                mStartState = true;
                mPauseState = false;
                mEndState = false;
                mNewState = false;
                break;
            case PAUSE_STATE:
                mStartState = false;
                mPauseState = true;
                mEndState = false;
                mNewState = false;
                break;
            case END_STATE:
                mStartState = false;
                mPauseState = false;
                mEndState = true;
                mNewState = false;
                break;
            case NEW_STATE:
                mStartState = false;
                mPauseState = false;
                mEndState = false;
                mNewState = true;
                break;
            default:
                Log.d(LOG_TAG, "Need to select correct state");
                break;
        }
    }


    //reset score and lives
    //all white circles are removed, player can place white circles on screen by touching, longer
    // the press the larger the circle
    private void newGame() {
        mCurrentScore = STARTING_SCORE;
        mCurrentLives = LIVES;
        createPlayer();

    }

    // all white circles start falling off the screen from top to bottom
    // each time the white circle is avoided, its fall speed increases 25% and its point value
    // increases by 1. This gets reset when the screen is cleared of white circles
    @SuppressLint("ClickableViewAccessibility")
    private void startGame() {
        mPlayerView.setVisibility(View.VISIBLE);

        //start the animation for the white circles
//        startAnimation(mPlayerView);
        // display the black circle with onTouch listener to listen for player action
        mGameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                eventX = event.getX();

                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                int maskedAction = event.getActionMasked();

                if (mStartState) {
                    switch (maskedAction) {
                        case MotionEvent.ACTION_DOWN:
                            mIsInMotion = true;
                            movePlayerCircle(eventX);
                            break;
                        case MotionEvent.ACTION_POINTER_DOWN:
                            mIsInMotion = false;
                            break;
                        case MotionEvent.ACTION_POINTER_UP:
                            mIsInMotion = true;
                            eventX = event.getX();
                            movePlayerCircle(eventX);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            //stop moving the circle
                            mIsInMotion = false;
                            Log.d(LOG_TAG, "ACTION UP");
                            break;
                    }
                }
                else if (mNewState) {
                    switch (maskedAction) {
                        case MotionEvent.ACTION_DOWN:
                            // create white circle and increase its radius
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            //
                            break;
                    }
                }
                mPlayerView.invalidate();
                return true;
            }
        });

    }

    // white circles stop moving and player should not be able to move black circle
    private void pauseGame() {
        // should not allow player action besides command bar
    }

    // ends the game. Maybe display the score in center if have time
    private void endGame() {
        // remove black circle
        mPlayerView.setVisibility(View.INVISIBLE);
    }


    private void updateCommandBar() {
        updateButtons();
        showScoreLives();
        Log.d(LOG_TAG, "Current State: New= "+mNewState+ ", Start= " +mStartState+ ", " +
                "Pause= " + mPauseState +", End= " +mEndState);
    }

    private void updateButtons() {
       if (mNewState) {
           mLeftButton.setVisibility(View.VISIBLE);
           mLeftButton.setText(R.string.pause);
           mRightButton.setText(R.string.start);
       }
       else if (mStartState) {
           mLeftButton.setText(R.string.pause);
           mRightButton.setText(R.string.end);
       }
       else if (mPauseState) {
           mLeftButton.setText(R.string.resume);
           mRightButton.setText(R.string.end);
       }
       else if (mEndState) {
           mLeftButton.setVisibility(View.INVISIBLE);
           mRightButton.setText(R.string.new_string);
       }
       else {
           Log.d(LOG_TAG, "Error, none of the state is set to true.");
       }
    }

    private void showScoreLives() {
        mScoreView.setText(String.valueOf(mCurrentScore));
        mLivesView.setText(String.valueOf(mCurrentLives));
    }

    // should move the player circle to the left if left side of screen is touched, and move
    // right if right side of screen is touched
    private void movePlayerCircle(float eventX) {
        if (eventX < (mScreenWidth / 2)) {
            Log.d(LOG_TAG, "Start State, Action Down on left side");
            if (mPlayerView.getX() >= 10) {
                    mPlayerView.setX(mPlayerView.getX() - 10);
                if (mIsInMotion) {
                    mPlayerView.postDelayed(new Mover(), 20);
                }
            }
        } else if (eventX >= mScreenWidth / 2) {
            Log.d(LOG_TAG, "Start State, Action Down on right side");
            if (mPlayerView.getX() + mPlayerView.getWidth() <= mScreenWidth-10) {
                mPlayerView.setX(mPlayerView.getX() + 10);
                if (mIsInMotion) {
                    mPlayerView.postDelayed(new Mover(), 20);
                }
            }
        }
        else {
            Log.d(LOG_TAG, "Start State, Action Down cannot move player circle");
            mIsInMotion = false;
        }
    }

    private boolean isInBounds(View widget) {
        float x = widget.getX();
        if (x >= 10 && (x + widget.getWidth() < mScreenWidth-10)) return true;
        return false;
    }

    private void getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    private void createPlayer() {
        mPlayerView = new LinearLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(PLAYER_W,PLAYER_H);
        mPlayerView.setLayoutParams(layoutParams);
        mPlayerView.setBackgroundResource(R.drawable.circle_shape);
        mPlayerView.setX(mPlayerX);
        mPlayerView.setY(mPlayerY);
        mGameView.addView(mPlayerView);
        mPlayerView.setVisibility(View.INVISIBLE);
    }

    private void startAnimation(View view) {
        float start = view.getTop();
        float end = mGameView.getHeight();

        ObjectAnimator circleAnimator = ObjectAnimator.ofFloat(view, "y", start, end);
        circleAnimator.setInterpolator(new AccelerateInterpolator());
        circleAnimator.setDuration(3000);
        circleAnimator.start();
    }

    class Mover implements Runnable {
        @Override
        public void run() {
            movePlayerCircle(eventX);
        }
    }
}
