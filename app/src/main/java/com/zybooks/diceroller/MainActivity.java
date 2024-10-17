package com.zybooks.diceroller;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements RollLengthDialogFragment.OnRollLengthSelectedListener {

    public static final int MAX_DICE = 3;

    private int mVisibleDice;
    private Dice[] mDice;
    private ImageView[] mDiceImageViews;
    private Menu mMenu;
    private CountDownTimer mTimer;
    private int mTimerLength = 2000;
    private int mCurrentDie;
    private GestureDetectorCompat mDetector;

    @Override
    public void onRollLengthClick(int which) {
        mTimerLength = 1000 * (which + 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an array of Dice
        mDice = new Dice[MAX_DICE];
        for (int i = 0; i < MAX_DICE; i++) {
            mDice[i] = new Dice(i + 1);
        }

        // Create an array of ImageViews
        mDiceImageViews = new ImageView[MAX_DICE];
        mDiceImageViews[0] = findViewById(R.id.dice1);
        mDiceImageViews[1] = findViewById(R.id.dice2);
        mDiceImageViews[2] = findViewById(R.id.dice3);

        // All dice are initially visible
        mVisibleDice = MAX_DICE;

        showDice();

        // Register context menus for all dice and tag each die
        for (int i = 0; i < mDiceImageViews.length; i++) {
            registerForContextMenu(mDiceImageViews[i]);
            mDiceImageViews[i].setTag(i);
        }

        mDiceImageViews[0].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });

        mDetector = new GestureDetectorCompat(this, new DiceGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class DiceGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            rollDice();
            return true;
        }
    }

    private void showDice() {
        // Display only the number of dice visible
        for (int i = 0; i < mVisibleDice; i++) {
            Drawable diceDrawable = ContextCompat.getDrawable(this, mDice[i].getImageId());
            mDiceImageViews[i].setImageDrawable(diceDrawable);
            mDiceImageViews[i].setContentDescription(Integer.toString(mDice[i].getNumber()));
        }
    }

    private void rollDice() {
        mMenu.findItem(R.id.action_stop).setVisible(true);

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new CountDownTimer(mTimerLength, 100) {
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mVisibleDice; i++) {
                    mDice[i].roll();
                }
                showDice();
            }

            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
            }
        }.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Determine which menu option was chosen
        int id = item.getItemId();

        if (id == R.id.action_roll_length) {
            FragmentManager manager = getSupportFragmentManager();
            RollLengthDialogFragment dialog = new RollLengthDialogFragment();
            dialog.show(manager, "rollLengthDialog");
            return true;
        } else if (id == R.id.action_stop) {
            mTimer.cancel();
            item.setVisible(false);
            return true;
        } else if (id == R.id.action_roll) {
            rollDice();
            return true;
        } else if (id == R.id.action_one) {
            changeDiceVisibility(1);
            showDice();
            return true;
        } else if (id == R.id.action_two) {
            changeDiceVisibility(2);
            showDice();
            return true;
        } else if (id == R.id.action_three) {
            changeDiceVisibility(3);
            showDice();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mCurrentDie = (int) v.getTag();   // Which die is selected?
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.add_one) {
            mDice[mCurrentDie].addOne();
            showDice();
            return true;
        } else if (id == R.id.subtract_one) {
            mDice[mCurrentDie].subtractOne();
            showDice();
            return true;
        } else if (id == R.id.roll) {
            rollDice();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }

    }

    private void changeDiceVisibility(int numVisible) {
        mVisibleDice = numVisible;

        // Make dice visible
        for (int i = 0; i < numVisible; i++) {
            mDiceImageViews[i].setVisibility(View.VISIBLE);
        }

        // Hide remaining dice
        for (int i = numVisible; i < MAX_DICE; i++) {
            mDiceImageViews[i].setVisibility(View.GONE);
        }
    }

}