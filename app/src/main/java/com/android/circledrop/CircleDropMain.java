package com.android.circledrop;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

    public class CircleDropMain extends AppCompatActivity {

        private final static String LOG_TAG = "CircleDropMain";
        private final static int LIVES = 3;
    private final static int STARTING_SCORE = 0;
    private final static String START_STATE = "startstate";
    private final static String PAUSE_STATE = "pausestate";
    private final static String END_STATE = "endstate";
    private final static String NEW_STATE = "newstate";

    private RelativeLayout mGameView;
    private LinearLayout mCommandBarView;
    private Button mLeftButton;
    private Button mRightButton;
    private TextView mScoreView;
    private TextView mLivesView;
    private Circle mObstacleCircle;

    private int mCurrentScore;
    private int mCurrentLives;
    private boolean mStartState;
    private boolean mPauseState;
    private boolean mEndState;
    private boolean mNewState;
    private boolean mIsInMotion;
    private boolean mIsFingerDown;
    private boolean mEndGame;
    private int mScreenWidth;
    private int mScreenHeight;
    private float mEventX;
    private float mEventY;


    private GamePlayView mGamePlayView;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGameView = (RelativeLayout) findViewById(R.id.gameView);
        mCommandBarView = (LinearLayout) findViewById(R.id.commandBarView);
        mLeftButton = (Button) findViewById(R.id.leftButton);
        mRightButton = (Button) findViewById(R.id.rightButton);
        mScoreView = (TextView) findViewById(R.id.scoreView);
        mLivesView = (TextView) findViewById(R.id.livesView);

        getScreenSize();

        // first time a game is run, everything must be set manually
        changeStateTo(NEW_STATE);
        newGame();
        updateCommandBar();

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewState) {
                    changeStateTo(PAUSE_STATE);
                    pauseGame();
                    updateCommandBar();
                }
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

        // display the black circle with onTouch listener to listen for player action
        mGameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEventX = event.getX();
                mEventY = event.getY();

                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                int maskedAction = event.getActionMasked();

                // should move the player circle left and right on one finger press, but stops it
                // if another finger is pressed
                if (mStartState) {
                    switch (maskedAction) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(LOG_TAG, "StartState ACTION DOWN");
                            mIsInMotion = true;
                            movePlayerCircle(mEventX);
                            return true;
                        case MotionEvent.ACTION_POINTER_DOWN:
                            mIsInMotion = false;
                            break;
                        case MotionEvent.ACTION_POINTER_UP:
                            mIsInMotion = true;
                            mEventX = event.getX();
                            movePlayerCircle(mEventX);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            //stop moving the circle
                            mIsInMotion = false;
                            Log.d(LOG_TAG, "StartState ACTION UP");
                            break;
                    }
                }
                // create obstacle circle and increase its radius on finger down
                else if (mNewState) {
                    switch (maskedAction) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(LOG_TAG, "NewState ACTION DOWN");
                            mIsFingerDown = true;
                            mObstacleCircle = createObstacleCircle(mEventX, mEventY);
                            increaseRadius(mObstacleCircle);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            // stop increasing the radius of the obstacle circle
                            Log.d(LOG_TAG, "NewState ACTION UP");
                            mIsFingerDown = false;
                            break;
                    }
                }
            return false;
            }
        });

        // allows the GamePlayView to send the live score to Activity each time it changes
        mGamePlayView.setMessageSentListener(new GamePlayView.MessageSentListener() {
            @Override
            public void onMessageSent() {
                Log.d(LOG_TAG, "///////RECEIVED MESSAGE");
                mCurrentScore = mGamePlayView.getScore();
                mCurrentLives = mGamePlayView.getLives();
                if (mCurrentLives <= 0) {
                    changeStateTo(END_STATE);
                    endGame();
                }
                updateCommandBar();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        changeStateTo(PAUSE_STATE);
        pauseGame();
        updateCommandBar();
    }

    //should be called every time a button is clicked
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
                break;
        }
    }

    //reset score and lives
    //all obstacle circles are removed, player can place obstacle circles on screen by touching,
    // longer
    // the press the larger the circle
    private void newGame() {
        mCurrentScore = STARTING_SCORE;
        mCurrentLives = LIVES;
        updateCommandBar();
        // remove view if end game was previously pressed
        if (mEndGame) {
            ((ViewManager)mGamePlayView.getParent()).removeView(mGamePlayView);
        }
        //create an instance of the game and add it to the Activity
        createGamePlayView();

    }

    // all obstacle circles start falling off the screen from top to bottom
    // each time the obstacle circle is avoided, its fall speed increases 25% and its point value
    // increases by 1. This gets reset when the screen is cleared of obstacle circles
    private void startGame() {
        mGamePlayView.startAnimation();
        //check for collision, use thread to get message each time a collision occurs
        //keep track of points and lives by retrieving it periodically from GamePlayView?
//        if (mLiveScore) {
//            mCommandBarView.post(new LiveScore());
//        }
    }

    // obstacle circles stop moving and player should not be able to move black circle
    private void pauseGame() {
        mGamePlayView.pauseAnimation();
        // should not allow player action besides command bar
    }

    // ends the game. Maybe display the score in center if have time
    private void endGame() {
        mGamePlayView.pauseAnimation();
        mEndGame = true;
        updateCommandBar();
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

    /*
    // should move the player circle to the left if left side of screen is touched, and move
    // right if right side of screen is touched
    private void movePlayerCircle(float eventX) {
        if (eventX < (mScreenWidth / 2)) {
            Log.d(LOG_TAG, "Start State, Action Down on left side");
            if (mPlayerView.getX() >= 10) {
                    mPlayerView.setX(mPlayerView.getX() - 10);
                if (mIsInMotion) {
//                    mPlayerView.postDelayed(new Mover(), 20);
                    mPlayerView.post(new Mover());
                }
            }
        } else if (eventX >= mScreenWidth / 2) {
            Log.d(LOG_TAG, "Start State, Action Down on right side");
            if (mPlayerView.getX() + mPlayerView.getWidth() <= mScreenWidth-10) {
                mPlayerView.setX(mPlayerView.getX() + 10);
                if (mIsInMotion) {
//                    mPlayerView.postDelayed(new Mover(), 20);
                    mPlayerView.post(new Mover());
                }
            }
        }
        else {
            Log.d(LOG_TAG, "Start State, Action Down cannot move player circle");
            mIsInMotion = false;
        }
    }
    */

    private void getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    private void createGamePlayView() {
        mGamePlayView = new GamePlayView(this, mScreenWidth, mScreenHeight, mCurrentScore, mCurrentLives);
        mGameView.addView(mGamePlayView);
    }

    private Circle createObstacleCircle(float eventX, float eventY) {
        Log.d(LOG_TAG, "createWhiteCircle() called");
        return mGamePlayView.createObstacleCircle(eventX, eventY);
    }

    private void increaseRadius(Circle circle) {
        if (circle != null) {
            mGamePlayView.increaseRadius(circle);
            if (mIsFingerDown) {
                mGamePlayView.post(new Enlarger());
            }
        }
        else if (circle == null){
            Toast.makeText(getApplicationContext(), R.string.circle_limit_string, Toast.LENGTH_SHORT).show();
        }
    }

    private void movePlayerCircle(float eventX) {
        Log.d(LOG_TAG, "movePlayerCircle() called");
        mGamePlayView.movePlayerCircle(eventX);
        if (mIsInMotion) {
            mGamePlayView.post(new Mover());
        }
    }

    class Mover implements Runnable {
        @Override
        public void run() {
            movePlayerCircle(mEventX);
        }
    }

    class Enlarger implements Runnable {
        @Override
        public void run() {
            increaseRadius(mObstacleCircle);
        }
    }

}
