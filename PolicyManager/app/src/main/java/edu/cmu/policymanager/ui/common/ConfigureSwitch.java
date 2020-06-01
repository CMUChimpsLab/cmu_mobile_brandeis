package edu.cmu.policymanager.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.datastructures.ConsistentStateTree;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 8/1/2018.
 *
 * Three-way policy control switch. Assumes one of three values:
 *  - On (allows access to data for a given purpose by one or all apps on this device)
 *  - Ask (prompt the user for a policy decision when this sensitive data is accessed by an app)
 *  - Off (denies access to data for a given purpose by one or all apps on this device)
 *  
 *  Controls can also allow or deny access based on who is accessing the data
 *  (app vs third-party library), which is determined by the values in the UserPolicy object
 *  the switch controls.
 * 
 * Parts of the Android Switch were re-used or adapted to make this component. See:
 * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/Switch.java
 * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/CompoundButton.java
 * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/util/MathUtils.java
 */

public class ConfigureSwitch extends View implements ConsistentStateTree.State {
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;

    private int mTouchMode;
    private int mTouchSlop;
    private float lastPressedX;
    private float mTouchY;
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private int mMinFlingVelocity;
    private float mThumbPosition = -1f;
    private boolean mChecked;

    private static final String sErrorMissingPolicy =
            "This control has a null policy. Did you remember to call setPolicy?";

    private final Context mContext;

    private boolean mEnabled = true,
                    mIsMasterSwitch = false;

    private ConsistentStateTree mStateTree;

    private int mBoxWidth = 300, mBoxHeight = 150, mOptionRadius = (mBoxHeight / 5);
    private float mThumbRadius = (1.50f * mOptionRadius),
                  mTextSize = 55,
                  mCircleY = ((mBoxHeight / 2) + (mTextSize / 2)),
                  mOnPosition = 0,
                  mAskPosition = 0,
                  mOffPosition = 0;

    private float mBarX1 = 0, mBarX2 = 0, mBarY1 = 0, mBarY2 = 0;

    private Paint mTrackColor = new Paint(),
                  mSelectorColor = new Paint(),
                  mTextStyle = new Paint(),
                  mTextSelectedStyle = new Paint();

    private byte mSwitchPolicyState = 0;
    private final byte STATE_ON = 1,
                       STATE_ASK = 2,
                       STATE_OFF = 4;

    public UserPolicy policy = null;

    public ConfigureSwitch(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public ConfigureSwitch(Context context,
                           AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    public ConfigureSwitch(Context context,
                           AttributeSet attrs,
                           int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context);
    }

    public Byte value() { return mSwitchPolicyState; }

    public void update(Object updatedState) {
        mSwitchPolicyState = (Byte)updatedState;
        adjustThumbToPolicyState();
    }

    public boolean equals(Object o) {
        ConfigureSwitch other = (ConfigureSwitch)o;
        return this.policy.equals(other.policy);
    }

    public void setReferenceToContainingTree(ConsistentStateTree tree) {
        mStateTree = tree;
    }

    /**
     * Set the user policy that this switch will control.
     *
     * @param policy the policy to control.
     * */
    public void setPolicy(final UserPolicy policy) {
        Precondition.checkUiThread();
        Precondition.checkIfNull(policy, "Cannot set a null policy");

        this.policy = policy;
        putThumbOnPolicy(policy);
    }

    /**
     * Align the switch thumb to the policy action (on/ask/allow) dictated by the input
     * user policy. Aligning the switch thumb does not alter the policy this current
     * switch controls. Its a visualization mechanism to let the user know what policy this
     * switch is currently influenced by.
     *
     * @param policy the policy to align this thumb to.
     * */
    public void putThumbOnPolicy(final UserPolicy policy) {
        Precondition.checkUiThread();
        Precondition.checkIfNull(policy, "Cannot set thumb on null policy");

        if(policy.isAllowed()) {
            mSwitchPolicyState = STATE_ON;
            mThumbPosition = mOnPosition;
        } else if(policy.isAsk()) {
            mSwitchPolicyState = STATE_ASK;
            mThumbPosition = mAskPosition;
        } else if(policy.isDenied()) {
            mSwitchPolicyState = STATE_OFF;
            mThumbPosition = mOffPosition;
        }

        invalidate();
    }

    private void adjustThumbToPolicyState() {
        Precondition.checkUiThread();

        if(mSwitchPolicyState == STATE_ON) {
            mThumbPosition = mOnPosition;
            policy.allow();
        } else if(mSwitchPolicyState == STATE_ASK) {
            mThumbPosition = mAskPosition;
            policy.ask();
        } else if(mSwitchPolicyState == STATE_OFF) {
            mThumbPosition = mOffPosition;
            policy.deny();
        }

        invalidate();
        PolicyManager.getInstance().update(policy);
    }

    boolean thumbPositioned = false;

    private void repositionThumbBasedOnPolicy() {
        if(!thumbPositioned) {
            if(policy != null) {
                if (policy.isAllowed()) { mThumbPosition = mOnPosition; }
                else if (policy.isAsk()) { mThumbPosition = mAskPosition; }
                else if (policy.isDenied()) { mThumbPosition = mOffPosition; }
                else {
                    disabledByError();
                    PolicyManagerDebug.debugWithMessage("policy-error: " + policy.toString());
                }
            } else {
                disabledByError();
                PolicyManagerDebug.debugWithMessage("Policy is null when placing thumb");
            }
        }

        thumbPositioned = true;
    }

    /**
     * Indicate to the user this switch controls other switches on
     * this screen by appending 'All' to each setting.
     * */
    public void renderAsMasterSwitch() { mIsMasterSwitch = true; }

    private void notifyUserOfPolicyChange() {
        String toastMessage = "";

        if(policy != null) {
            String appName = policy.app.equals(PolicyManagerApplication.SYMBOL_ALL) ?
                    "All apps" : Util.getAppCommonName(mContext, policy.app);

            String permission = policy.permission.getDisplayPermission().toString();

            String purpose = policy.purpose.name.equals(PolicyManagerApplication.SYMBOL_ALL) ?
                    "all purposes" : policy.purpose.name.toString();

            String usedBy = "";

            if (policy.thirdPartyLibrary != null &&
                !policy.thirdPartyLibrary.equals(ThirdPartyLibraries.ALL)) {
                usedBy = " by " + policy.thirdPartyLibrary.name;
            }

            if (policy.isAllowed()) {
                toastMessage = appName + " will be allowed access to " +
                        permission + " for " + purpose + usedBy;
            } else if (policy.isAsk()) {
                toastMessage = "You will be prompted to decide when running the app";
            } else if (policy.isDenied()) {
                toastMessage = appName + " will be denied access to " +
                               permission + " for " + purpose + usedBy;
            }
        }

        if(mEnabled) {
            Toast.makeText(mContext, toastMessage, Toast.LENGTH_LONG).show();
        } else {
            toastMessage = "This control is currently disabled";
            Toast.makeText(mContext, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set the user policy this switch controls to allow this data access,
     * and update the switch state to 'On'.
     * */
    public void setToOn() {
        Precondition.checkUiThread();

        if(mEnabled && mSwitchPolicyState != STATE_ON) {
            if(policy != null) {
                mSwitchPolicyState = STATE_ON;
                mThumbPosition = mOnPosition;
                invalidate();

                policy.allow();
                PolicyManager.getInstance().update(policy);

                mStateTree.propagateStateChange(STATE_ON);
            } else {
                throw new IllegalStateException(sErrorMissingPolicy);
            }
        }
    }

    /**
     * Set the user policy this switch controls to prompt the user for a decison when this data
     * is accessed, and update the switch state to 'Ask'.
     * */
    public void setToAsk() {
        Precondition.checkUiThread();

        if(mEnabled && mSwitchPolicyState != STATE_ASK) {
            if(policy != null) {
                mSwitchPolicyState = STATE_ASK;
                mThumbPosition = mAskPosition;
                invalidate();

                policy.ask();
                PolicyManager.getInstance().update(policy);

                mStateTree.propagateStateChange(STATE_ASK);
            } else {
                throw new IllegalArgumentException(sErrorMissingPolicy);
            }
        }
    }

    /**
     * Set the user policy this switch controls to deny access to this data, and update the
     * switch state to 'Off'.
     * */
    public void setToOff() {
        Precondition.checkUiThread();

        if(mEnabled && mSwitchPolicyState != STATE_OFF) {
            if(policy != null) {
                mSwitchPolicyState = STATE_OFF;
                mThumbPosition = mOffPosition;
                invalidate();

                policy.deny();
                PolicyManager.getInstance().update(policy);

                mStateTree.propagateStateChange(STATE_OFF);
            } else {
                throw new IllegalArgumentException(sErrorMissingPolicy);
            }
        }
    }

    /**
     * Allows for this control to be used, or the thumb slider to move. The control will
     * assume its normal color of a blue thumb.
     * */
    public void enable() {
        mEnabled = true;

        mSelectorColor.setColor(Color.rgb(45, 137, 255));
        mSelectorColor.setStyle(Paint.Style.FILL);

        mTextStyle.setColor(Color.BLACK);
        mTextStyle.setTextSize(mTextSize);
        mTextStyle.setStyle(Paint.Style.FILL);

        mTextSelectedStyle.setColor(Color.rgb(45, 137, 255));
        mTextSelectedStyle.setTextSize(mTextSize);
        mTextSelectedStyle.setTypeface(Typeface.DEFAULT_BOLD);
        mTextSelectedStyle.setStyle(Paint.Style.FILL);
    }

    /**
     * Disables the control by making it non-responsive to user attempts at moving the thumb,
     * and renders the thumb as grey.
     * */
    public void disable() {
        mEnabled = false;

        mSelectorColor.setColor(Color.rgb(189,189,189));
        mSelectorColor.setStyle(Paint.Style.FILL);

        mTextStyle.setColor(Color.rgb(189,189,189));
        mTextStyle.setTextSize(mTextSize);
        mTextStyle.setStyle(Paint.Style.FILL);

        mTextSelectedStyle.setColor(Color.rgb(189,189,189));
        mTextSelectedStyle.setTextSize(mTextSize);
        mTextSelectedStyle.setTypeface(Typeface.DEFAULT_BOLD);
        mTextSelectedStyle.setStyle(Paint.Style.FILL);
    }

    /**
     * Disables the control by making it non-responsive to user attempts at moving the thumb,
     * and renders the thumb as red. Should only be used if an error is detected which
     * renders this control unusable.
     * */
    public void disabledByError() {
        mEnabled = false;
        mThumbPosition = mOffPosition;

        mSelectorColor.setColor(Color.rgb(255,70,70));
        mSelectorColor.setStyle(Paint.Style.FILL);

        mTextStyle.setColor(Color.rgb(255,70,70));
        mTextStyle.setTextSize(mTextSize);
        mTextStyle.setStyle(Paint.Style.FILL);

        mTextSelectedStyle.setColor(Color.rgb(255,70,70));
        mTextSelectedStyle.setTextSize(mTextSize);
        mTextSelectedStyle.setTypeface(Typeface.DEFAULT_BOLD);
        mTextSelectedStyle.setStyle(Paint.Style.FILL);
    }

    private boolean mViewWasNotResized = true;

    private void resizeViewIfNecessary() {
        if(mViewWasNotResized) {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int)(params.height + mTextSize + 10);
            setLayoutParams(params);

            mCircleY = mCircleY + (mTextSize / 2) + 35;
            mBarY1 = mBarY1 + (mTextSize / 2) + 35;
            mBarY2 = mBarY2 + (mTextSize / 2) + 35;

            mViewWasNotResized = false;
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mIsMasterSwitch) {
            resizeViewIfNecessary();
        }

        repositionThumbBasedOnPolicy();

        canvas.drawRect(mBarX1, mBarY1, mBarX2, mBarY2, mTrackColor);
        canvas.drawCircle(mOffPosition, mCircleY, mOptionRadius, mTrackColor);
        canvas.drawCircle(mAskPosition, mCircleY, mOptionRadius, mTrackColor);
        canvas.drawCircle(mOnPosition, mCircleY, mOptionRadius, mTrackColor);

        canvas.drawCircle(mThumbPosition, mCircleY, mThumbRadius, mSelectorColor);

        drawLabelsAndBoldSelected(canvas);
    }

    private void drawLabelsAndBoldSelected(final Canvas canvas) {
        if(mIsMasterSwitch) {
            drawMasterSwitchLabel(canvas);
        }

        drawPolicyActionLabels(canvas);
    }

    private void drawMasterSwitchLabel(final Canvas canvas) {
        float onOffset = (mOnPosition - (int)(mTextSize * 0.65)),
              askOffset = ((mAskPosition - (int)(mTextSize * 0.65)) + 10),
              offOffset = ((mOffPosition - (int)(mTextSize * 0.65)) + 5);

        if(mThumbPosition == mOnPosition) {
            canvas.drawText("All", onOffset, mTextSize, mTextSelectedStyle);
            canvas.drawText("All", askOffset, mTextSize, mTextStyle);
            canvas.drawText("All", offOffset, mTextSize, mTextStyle);
        } else if(mThumbPosition == mAskPosition) {
            canvas.drawText("All", onOffset, mTextSize, mTextStyle);
            canvas.drawText("All", askOffset, mTextSize, mTextSelectedStyle);
            canvas.drawText("All", offOffset, mTextSize, mTextStyle);
        } else {
            canvas.drawText("All", onOffset, mTextSize, mTextStyle);
            canvas.drawText("All", askOffset, mTextSize, mTextStyle);
            canvas.drawText("All", offOffset, mTextSize, mTextSelectedStyle);
        }
    }

    private void drawPolicyActionLabels(final Canvas canvas) {
        float labelY = mTextSize;

        if(mIsMasterSwitch) { labelY = (mTextSize * 2); }

        float onOffset = (mOnPosition - (int)(mTextSize * 0.65)),
                askOffset = (mAskPosition - (int)(mTextSize * 0.65)),
                offOffset = (mOffPosition - (int)(mTextSize * 0.65));

        if(mThumbPosition == mOnPosition) {
            canvas.drawText("On", onOffset, labelY, mTextSelectedStyle);
            canvas.drawText("Ask", askOffset, labelY, mTextStyle);
            canvas.drawText("Off", offOffset, labelY, mTextStyle);
        } else if(mThumbPosition == mAskPosition) {
            canvas.drawText("On", onOffset, labelY, mTextStyle);
            canvas.drawText("Ask", askOffset, labelY, mTextSelectedStyle);
            canvas.drawText("Off", offOffset, labelY, mTextStyle);
        } else {
            canvas.drawText("On", onOffset, labelY, mTextStyle);
            canvas.drawText("Ask", askOffset, labelY, mTextStyle);
            canvas.drawText("Off", offOffset, labelY, mTextSelectedStyle);
        }
    }

    /* https://stackoverflow.com/questions/18996183/identifying-rtl-language-in-android/18996319 */
    private boolean isLayoutRtl() { return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL; }

    private void stopDrag(MotionEvent ev) {
        mTouchMode = TOUCH_MODE_IDLE;
        // Commit the change if the event is up and not canceled and the switch
        // has not been disabled during the drag.
        final boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP;
        final boolean oldState = mChecked;
        final boolean newState;
        if (commitChange) {
            mVelocityTracker.computeCurrentVelocity(1000);
            final float xvel = mVelocityTracker.getXVelocity();
            if (Math.abs(xvel) > mMinFlingVelocity) {
                newState = isLayoutRtl() ? (xvel < 0) : (xvel > 0);
            } else {
                newState = getTargetCheckedState();
            }
        } else {
            newState = oldState;
        }
        if (newState != oldState) {
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        // Always call setChecked so that the thumb is moved back to the correct edge
        mChecked = newState;
        cancelSuperTouch(ev);
    }

    private boolean getTargetCheckedState() {
        return mThumbPosition > 0.5f;
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    private void setThumbPosition(float position) {
        mThumbPosition = (position - mThumbRadius);

        if(mThumbPosition == mOnPosition) {
            setToOn();
            notifyUserOfPolicyChange();
        } else if(mThumbPosition == mAskPosition) {
            setToAsk();
            notifyUserOfPolicyChange();
        } else if(mThumbPosition == mOffPosition) {
            setToOff();
            notifyUserOfPolicyChange();
        }
    }

    private boolean hitThumb(float x, float y) {
        final int thumbTop = (int)(mCircleY - mThumbRadius - mTouchSlop);
        final int thumbLeft = (int)(mThumbPosition - mThumbRadius  - mTouchSlop);
        final int thumbRight = (int)(thumbLeft + (2 * mThumbRadius) + mTouchSlop);
        final int thumbBottom = (int)(thumbTop + (2 * mThumbRadius) + mTouchSlop);

        return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                if (hitThumb(x, y)) {
                    mTouchMode = TOUCH_MODE_DOWN;
                    lastPressedX = x;
                    mTouchY = y;
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                switch (mTouchMode) {
                    case TOUCH_MODE_IDLE:
                        // Didn't target the thumb, treat normally.
                        break;

                    case TOUCH_MODE_DOWN: {
                        final float x = ev.getX();
                        final float y = ev.getY();

                        if (Math.abs(x - lastPressedX) > mTouchSlop ||
                                Math.abs(y - mTouchY) > mTouchSlop) {
                            mTouchMode = TOUCH_MODE_DRAGGING;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            lastPressedX = x;
                            mTouchY = y;
                            return true;
                        }

                        break;
                    }
                    case TOUCH_MODE_DRAGGING: {
                        final float x = ev.getX();
                        final float thumbScrollOffset = x - lastPressedX;
                        float dPos;

                        dPos = thumbScrollOffset;

                        if (isLayoutRtl()) {
                            dPos = -dPos;
                        }

                        final float newPos = constrain(mThumbPosition + dPos);

                        if (newPos != mThumbPosition) {
                            lastPressedX = x;
                            setThumbPosition(newPos + mThumbRadius);
                        }

                        return true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    stopDrag(ev);
                    // Allow super class to handle pressed state, etc.
                    super.onTouchEvent(ev);
                    return true;
                }
                mTouchMode = TOUCH_MODE_IDLE;
                mVelocityTracker.clear();
                break;
            }
        }

        return true;
    }

    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        mBoxWidth = MeasureSpec.getSize(widthMeasureSpec);
        mBoxHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(mViewWasNotResized) {
            mOptionRadius = (mBoxHeight / 5);
            mThumbRadius = (1.50f * mOptionRadius);

            mBarX1 = mThumbRadius;
            mBarX2 = (mBoxWidth - mThumbRadius);

            mCircleY = ((mBoxHeight / 2) + (mTextSize / 2));
            mBarY1 = (mCircleY - (mOptionRadius / 2));
            mBarY2 = (mBarY1 + mOptionRadius);

            mAskPosition = (mThumbRadius + ((mBarX2 - mBarX1) / 2));
            mOnPosition = mBarX2;
            mOffPosition = mThumbRadius;
        }

        setMeasuredDimension(mBoxWidth, mBoxHeight);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBoxWidth = w;
        mBoxHeight = h;
    }

    private void init(final Context context) {
        Precondition.checkUiThread();

        mTrackColor.setColor(Color.rgb(228, 228, 228));
        mTrackColor.setStyle(Paint.Style.FILL);

        mSelectorColor.setColor(Color.rgb(45, 137, 255));
        mSelectorColor.setStyle(Paint.Style.FILL);

        mTextStyle.setColor(Color.rgb(172, 172, 172));
        mTextStyle.setTextSize(mTextSize);
        mTextStyle.setStyle(Paint.Style.FILL);

        mTextSelectedStyle.setColor(Color.BLACK);
        mTextSelectedStyle.setTextSize(mTextSize);
        mTextSelectedStyle.setTypeface(Typeface.DEFAULT_BOLD);
        mTextSelectedStyle.setStyle(Paint.Style.FILL);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMinFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    private float constrain(float amount) {
        if(amount <= mOffPosition) { return mThumbRadius; }
        if(amount >= mOnPosition) { return mOnPosition; }

        boolean dragIsTowardsOff = (amount < ((mAskPosition - mOffPosition) / 2));
        boolean dragIsTowardsOn = (amount > (mAskPosition + ((mOnPosition - mAskPosition) / 2)));

        if(dragIsTowardsOff) { return mOffPosition; }
        if(dragIsTowardsOn) { return mOnPosition; }

        return mAskPosition;
    }
}