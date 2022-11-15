package cat.merino.albert.laserslides;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


/**
 * ButtonOSCWrappper
 * This is the wrapper class for Button controls.
 * Stores OSC messages, and manages the touch listener to pass OSC messages to the parent activity.
 * Messages are triggered when button pressed and button released.
 * With the triggerWhenButtonReleased flag, release action can be discarded.
 *
 * @author ahmetkizilay
 */
public class ButtonOSCWrapper implements OnTouchListener {

    private Button button;
    private QuickOSCActivity parentActivity;

    private int index = 0;

    private String messageButtonPressedAddr = "";
    private List<Object> messageButtonPressedArgs = null;
    private String messageButtonPressedRaw;

    private String messageButtonReleasedAddr = "";
    private List<Object> messageButtonReleasedArgs = null;
    private String messageButtonReleasedRaw;

    private String name;
    private boolean triggerWhenButtonReleased = true;


    private ButtonOSCWrapper(int index, String name, String msgButtonPressed, boolean trigWhenButtonReleased, String msgButtonReleased, Button button, QuickOSCActivity parentActivity) {
        this.index = index;
        this.button = button;
        this.name = name;
        this.parentActivity = parentActivity;

        this.setMessageButtonPressed(msgButtonPressed);
        this.setMessageButtonReleased(msgButtonReleased);

        this.triggerWhenButtonReleased = trigWhenButtonReleased;
        this.button.setOnTouchListener(this);
        this.button.setText(this.name);
    }


    public static ButtonOSCWrapper createInstance(int index, String name, String msgButtonPressed, boolean trigWhenButtonReleased, String msgButtonReleased, Button button, QuickOSCActivity parentActivity) {
        return new ButtonOSCWrapper(index, name, msgButtonPressed, trigWhenButtonReleased, msgButtonReleased, button, parentActivity);
    }

    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!parentActivity.isEditMode()) {
                parentActivity.sendOSC(this.messageButtonPressedAddr, this.messageButtonPressedArgs);
                parentActivity.setDebugMessage(this.messageButtonPressedRaw);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (parentActivity.isEditMode()) {
                parentActivity.callButtonOSCSetter(this);
            } else if (this.triggerWhenButtonReleased) {
                parentActivity.sendOSC(this.messageButtonReleasedAddr, this.messageButtonReleasedArgs);
                parentActivity.setDebugMessage(this.messageButtonReleasedRaw);
            }
        } else {
            //	parentActivity.setDebugMessage(thisButton.getText() + " is " + event.getAction());
        }
        return false;
    }

    public void setMessageButtonPressed(String messageButtonPressed) {
        if (messageButtonPressed == null || messageButtonPressed.equals("")) {
            this.messageButtonPressedAddr = name + "/1";
            this.messageButtonPressedArgs = null;
            this.messageButtonPressedRaw = this.messageButtonPressedAddr;
        } else {
            this.messageButtonPressedRaw = messageButtonPressed;

            String[] msgButtonPressedParts = messageButtonPressed.split(" ");
            this.messageButtonPressedAddr = msgButtonPressedParts[0];
            if (msgButtonPressedParts.length > 0) {
                this.messageButtonPressedArgs = new ArrayList<Object>();
                for (int i = 1; i < msgButtonPressedParts.length; i++) {
                    this.messageButtonPressedArgs.add(Utils.simpleParse(msgButtonPressedParts[i]));
                }
            }
        }
    }

    public void setMessageButtonReleased(String messageButtonReleased) {
        if (messageButtonReleased == null || messageButtonReleased.equals("")) {
            this.messageButtonReleasedAddr = name + "/1";
            this.messageButtonReleasedArgs = null;
            this.messageButtonReleasedRaw = this.messageButtonReleasedAddr;
        } else {
            this.messageButtonReleasedRaw = messageButtonReleased;

            String[] msgButtonReleasedParts = messageButtonReleased.split(" ");
            this.messageButtonReleasedAddr = msgButtonReleasedParts[0];
            if (msgButtonReleasedParts.length > 0) {
                this.messageButtonReleasedArgs = new ArrayList<Object>();
                for (int i = 1; i < msgButtonReleasedParts.length; i++) {
                    this.messageButtonReleasedArgs.add(Utils.simpleParse(msgButtonReleasedParts[i]));
                }
            }
        }
    }

    public String getMessageButtonPressedRaw() {
        return this.messageButtonPressedRaw;
    }

    public String getMessageButtonReleasedRaw() {
        return this.messageButtonReleasedRaw;
    }

    public boolean getTriggerWhenButtonReleased() {
        return this.triggerWhenButtonReleased;
    }

    public void setTriggerWhenButtonReleased(boolean triggerWhenButtonReleased) {
        this.triggerWhenButtonReleased = triggerWhenButtonReleased;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                ButtonOSCWrapper.this.button.setText(ButtonOSCWrapper.this.name);
            }
        });

    }

    public int getIndex() {
        return this.index;
    }

}
