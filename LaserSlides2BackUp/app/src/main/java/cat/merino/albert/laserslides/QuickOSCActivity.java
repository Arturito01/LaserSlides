package cat.merino.albert.laserslides;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import cat.merino.albert.laserslides.fragments.NetworkDialogFragment;
import cat.merino.albert.laserslides.fragments.WifiSettingsDialogFragment;


/***
 * Entry point for the application.
 *
 * Each view (Button, Toggle and SeekBar) has a corresponding Wrapper object which takes care of storing 
 * OSC message strings, managing listeners and passing OSC messages to the parent activity class to be sent to the host.
 *
 *
 * @author ahmetkizilay
 *
 */
public class QuickOSCActivity extends AppCompatActivity {
    private final static int BUTTON_OSC_INTENT_RESULT = 1;
    private final static int TOGGLE_OSC_INTENT_RESULT = 2;
    private final static int SEEKBAR_OSC_INTENT_RESULT = 3;

    private final static String PROMO_DIALOG = "dlg-promo";
    private final static String NETWORK_DIALOG = "dlg-network";
    private final static String WIFI_ALERT_DIALOG = "dlg-wifi";

    private final static String NETWORK_SETTINGS_FILE = "qosc_network.cfg";
    private final static String OSC_SETTINGS_FILE = "qosc_osc.cfg";
    private final static String PREF_FILE = "qosc_pref";
    private final static String PROMO_SHOWN = "promo_shown";
    TextView debugTextView;
    private List<ButtonOSCWrapper> buttonOSCWrapperList = new ArrayList<ButtonOSCWrapper>();
    private List<ToggleOSCWrapper> toggleOSCWrapperList = new ArrayList<ToggleOSCWrapper>();
    private List<SeekBarOSCWrapper> seekBarOSCWrapperList = new ArrayList<SeekBarOSCWrapper>();
    private Hashtable<String, String> oscSettingsHashtable = new Hashtable<String, String>();
    private boolean editMode = false;
    private String ipAddress = "127.0.0.1";
    private int port = 8000;
    private OSCPortOut oscPortOut = null;
    private boolean mListenIncoming = true;
    private int inPort = 8090;
    public String OSC_START_PATH = "/";
    private OSCPortIn oscPortIn;
    private OSCListener btnListener;
    private OSCListener toggleListener;
    private OSCListener seekBarListener;
    private OSCListener saveListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        restoreOSCSettingsFromFile();

        debugTextView = (TextView) findViewById(R.id.textView1);

        int[] ids = {
                R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button4, R.id.button5, R.id.button6, R.id.button8,
                R.id.button7, R.id.button8, R.id.button9, R.id.button12,
                R.id.button10, R.id.button11, R.id.button12, R.id.button13,
                R.id.button14, R.id.button15,

        };
        for (int i = 0; i < ids.length; i++) {
            addButtonToList(ids[i], i);
        }


        // DEFINING OSC LISTENERS HERE
        this.btnListener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                String[] addressParts = message.getAddress().split("/");
                int index = extractIndex(addressParts[1], "btn");
                if (index < 1 || index > 16) {
                    return;
                }
                ButtonOSCWrapper btn = buttonOSCWrapperList.get(index - 1);

                String action = addressParts[2];

                if (action.equals("msgButtonPressed")) {
                    btn.setMessageButtonPressed(Utils.convertToString(message.getArguments()));
                } else if (action.equals("msgButtonReleased")) {
                    btn.setMessageButtonReleased(Utils.convertToString(message.getArguments()));
                } else if (action.equals("triggerOnButtonReleased")) {
                    // only check the first arguments
                    List<Object> arguments = message.getArguments();
                    if (arguments.size() > 0) {
                        Object value = arguments.get(0);
                        if (value instanceof Integer) {
                            int intValue = ((Integer) value).intValue();
                            if (intValue == 0) {
                                btn.setTriggerWhenButtonReleased(false);
                            } else if (intValue == 1) {
                                btn.setTriggerWhenButtonReleased(true);
                            }
                        }
                    }
                } else if (action.equals("label")) {
                    btn.setName(Utils.convertToString(message.getArguments()));
                }
            }
        };

        this.toggleListener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                String[] addressParts = message.getAddress().split("/");
                int index = extractIndex(addressParts[1], "tog");
                if (index < 1 || index > 8) {
                    return;
                }
                ToggleOSCWrapper btn = toggleOSCWrapperList.get(index - 1);

                String action = addressParts[2];

                if (action.equals("msgToggledOn")) {
                    btn.setMessageToggleOn(Utils.convertToString(message.getArguments()));
                } else if (action.equals("msgToggledOff")) {
                    btn.setMessageToggleOff(Utils.convertToString(message.getArguments()));
                } else if (action.equals("value")) {
                    // only check the first arguments
                    List<Object> arguments = message.getArguments();
                    if (arguments.size() > 0) {
                        Object value = arguments.get(0);
                        if (value instanceof Integer) {
                            int intValue = ((Integer) value).intValue();
                            if (intValue == 0) {
                                btn.setToggled(false);
                            } else if (intValue == 1) {
                                btn.setToggled(true);
                            }
                        }
                    }
                } else if (action.equals("labelOn")) {
                    btn.setOnLabel(Utils.convertToString(message.getArguments()));
                } else if (action.equals("labelOff")) {
                    btn.setOffLabel(Utils.convertToString(message.getArguments()));
                }
            }
        };

        this.seekBarListener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                String[] addressParts = message.getAddress().split("/");
                int index = extractIndex(addressParts[1], "sb");
                if (index < 1 || index > 4) {
                    return;
                }
                SeekBarOSCWrapper seekBar = seekBarOSCWrapperList.get(index - 1);

                String action = addressParts[2];

                if (action.equals("msgValueChanged")) {
                    seekBar.setMsgValueChanged(Utils.convertToString(message.getArguments()));
                } else if (action.equals("range")) {
                    List<Object> arguments = message.getArguments();
                    if (arguments.size() < 2) {
                        return;
                    }
                    float minValue = 0f;
                    float maxValue = 0f;
                    try {
                        if (arguments.get(0) instanceof Integer) {
                            minValue = (float) ((Integer) arguments.get(0)).intValue();
                        } else if (arguments.get(0) instanceof Float) {
                            minValue = ((Float) arguments.get(0)).floatValue();

                        } else if (arguments.get(0) instanceof Double) {
                            minValue = ((Double) arguments.get(0)).floatValue();
                        } else {
                            return;
                        }

                        if (arguments.get(1) instanceof Integer) {
                            maxValue = (float) ((Integer) arguments.get(1)).intValue();
                        } else if (arguments.get(1) instanceof Float) {
                            maxValue = ((Float) arguments.get(1)).floatValue();
                        } else if (arguments.get(1) instanceof Double) {
                            maxValue = ((Double) arguments.get(1)).floatValue();
                        } else {
                            return;
                        }
                    } catch (NumberFormatException nfe) {
                        return;
                    }
                    seekBar.setMinValue(minValue);
                    seekBar.setMaxValue(maxValue);
                } else if (action.equals("value")) {
                    List<Object> arguments = message.getArguments();
                    if (arguments.size() < 1) {
                        return;
                    }
                    float value = 0f;
                    try {
                        if (arguments.get(0) instanceof Integer) {
                            value = (float) ((Integer) arguments.get(0)).intValue();
                        } else if (arguments.get(0) instanceof Float) {
                            value = ((Float) arguments.get(0)).floatValue();
                        } else {
                            return;
                        }

                    } catch (NumberFormatException nfe) {
                        return;
                    }
                    seekBar.setValue(value);
                }
            }
        };

        this.saveListener = new OSCListener() {
            public void acceptMessage(Date date, OSCMessage oscMessage) {
                saveOSCSettingsIntoFile();
            }
        };
        checkWifiState();
    }

    private void addButtonToList(int id, int num) {
        String key = "btn" + (num + 1);
        Button button = (Button) findViewById(id);
        buttonOSCWrapperList.add(ButtonOSCWrapper.createInstance(num,
                oscSettingsHashtable.containsKey(key + "-lbl") ? oscSettingsHashtable.get(key + "-lbl") : key,
                oscSettingsHashtable.get(key + "-butpres"),
                Boolean.parseBoolean(oscSettingsHashtable.get(key + "-trgbutrel")),
                oscSettingsHashtable.get(key + "-butrel"),
                button, this));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.oscPortIn != null) {
            if (this.oscPortIn.isListening()) {
                this.oscPortIn.stopListening();
            }
            this.oscPortIn.close();
        }

        if (this.oscPortOut != null) {
            this.oscPortOut.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreNetworkSettingsFromFile();
        initializeOSC();
        initializeIncomingOSC();
    }

    public boolean isEditMode() {
        return this.editMode;
    }

    /***
     * Normally used to display the OSC message passed by the wrappers.
     * @param message
     */
    public void setDebugMessage(String message) {
        this.debugTextView.setText(message);
    }

    /***
     * In edit mode, the ButtonOSCWrapper calls this method and passes itself as the argument.
     * An intent object is created with the properties of the wrapper object attached.
     * Upon successful return of the intent result, the new values are passed back to the wrapper object
     * in the handleButtonOSCSettingResult method.
     * @param selectedButton
     */
    public void callButtonOSCSetter(ButtonOSCWrapper selectedButton) {
        try {
            //this.selectedButtonOSCWrapper = selectedButton;

            Intent intent = new Intent(this, ButtonOSCSettingActivity.class);
            intent.setAction("ButtonOSCSetter");
            intent.putExtra("label", selectedButton.getName());
            intent.putExtra("msgButtonPressed", selectedButton.getMessageButtonPressedRaw());
            intent.putExtra("msgButtonReleased", selectedButton.getMessageButtonReleasedRaw());
            intent.putExtra("trigButtonReleased", selectedButton.getTriggerWhenButtonReleased());
            intent.putExtra("index", selectedButton.getIndex());
            startActivityForResult(intent, BUTTON_OSC_INTENT_RESULT);
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(this, "Error calling ButtonOSCSetter Intent", Toast.LENGTH_LONG).show();
        }
    }

    /***
     * In edit mode, the ToggleOSCWrapper calls this method and passes itself as the argument.
     * An intent object is created with the properties of the wrapper object attached.
     * Upon successful return of the intent result, the new values are passed back to the wrapper object
     * in the handleToggleOSCSettingResult method.
     * @param selectedToggle
     */
    public void callToggleOSCSetter(ToggleOSCWrapper selectedToggle) {
        try {
            //selectedToggleOSCWrapper = selectedToggle;

            Intent intent = new Intent(this, ToggleOSCSettingActivity.class);
            intent.setAction("ToggleOSCSetter");
            intent.putExtra("onLabel", selectedToggle.getOnLabel());
            intent.putExtra("offLabel", selectedToggle.getOffLabel());
            intent.putExtra("msgToggleOn", selectedToggle.getMessageToggleOnRaw());
            intent.putExtra("msgToggleOff", selectedToggle.getMessageToggleOffRaw());
            intent.putExtra("index", selectedToggle.getIndex());
            startActivityForResult(intent, TOGGLE_OSC_INTENT_RESULT);
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(this, "Error calling ToggleOSCSetter Intent", Toast.LENGTH_LONG).show();
        }
    }

    /***
     * In edit mode, the SeekBarOSCWrapper calls this method and passes itself as the argument.
     * An intent object is created with the properties of the wrapper object attached.
     * Upon successful return of the intent result, the new values are passed back to the wrapper object
     * in the handleSeekBarOSCSettingResult method.
     * @param selectedSeekBar
     */
    public void callSeekBarOSCSetter(SeekBarOSCWrapper selectedSeekBar) {
        try {
            //selectedSeekBarOSCWrapper = selectedSeekBar;

            Intent intent = new Intent(this, SeekBarOSCSettingActivity.class);
            intent.putExtra("msgValueChanged", selectedSeekBar.getMsgValueChanged());
            intent.putExtra("maxValue", selectedSeekBar.getMaxValue());
            intent.putExtra("minValue", selectedSeekBar.getMinValue());
            intent.putExtra("index", selectedSeekBar.getIndex());

            intent.setAction("SeekBarOSCSetter");
            startActivityForResult(intent, SEEKBAR_OSC_INTENT_RESULT);
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(this, "Error calling SeekBarOSCSetter Intent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BUTTON_OSC_INTENT_RESULT:
                    handleButtonOSCSettingResult(data);
                    break;
                case TOGGLE_OSC_INTENT_RESULT:
                    handleToggleOSCSettingResult(data);
                    break;
                case SEEKBAR_OSC_INTENT_RESULT:
                    handleSeekBarOSCSettingResult(data);
                    break;
            }
        }
    }

    /**
     * The intent sent from the settings activity is passed into the caller wrapper object.
     *
     * @param intent
     */
    private void handleButtonOSCSettingResult(Intent intent) {

        String label = intent.getExtras().get("label").toString();
        String msgButtonPressed = intent.getExtras().get("msgButtonPressed").toString();
        String msgButtonReleased = (String) intent.getExtras().get("msgButtonReleased").toString();
        boolean trigButtonReleased = Boolean.parseBoolean(intent.getExtras().get("trigButtonReleased").toString());
        int indexSelectedButton = Integer.parseInt(intent.getExtras().get("index").toString());

        ButtonOSCWrapper selectedButton = this.buttonOSCWrapperList.get(indexSelectedButton);
        selectedButton.setName(label);
        selectedButton.setMessageButtonPressed(msgButtonPressed);
        selectedButton.setMessageButtonReleased(msgButtonReleased);
        selectedButton.setTriggerWhenButtonReleased(trigButtonReleased);

        saveOSCSettingsIntoFile();
    }

    /**
     * The intent sent from the settings activity is passed into the caller wrapper object.
     *
     * @param intent
     */
    private void handleToggleOSCSettingResult(Intent intent) {
        String onLabel = intent.getExtras().get("onLabel").toString();
        String offLabel = intent.getExtras().get("offLabel").toString();
        String msgToggleOn = intent.getExtras().get("msgToggleOn").toString();
        String msgToggleOff = intent.getExtras().get("msgToggleOff").toString();
        int selectedIndex = Integer.parseInt(intent.getExtras().get("index").toString());

        ToggleOSCWrapper selectedToggle = toggleOSCWrapperList.get(selectedIndex);
        selectedToggle.setOnLabel(onLabel);
        selectedToggle.setOffLabel(offLabel);
        selectedToggle.setMessageToggleOn(msgToggleOn);
        selectedToggle.setMessageToggleOff(msgToggleOff);

        saveOSCSettingsIntoFile();
    }

    /**
     * The intent sent from the settings activity is passed into the caller wrapper object.
     *
     * @param intent
     */
    private void handleSeekBarOSCSettingResult(Intent intent) {

        String msgValueChanged = intent.getExtras().get("msgValueChanged").toString();
        float fltMaxValue = Float.parseFloat(intent.getExtras().get("maxValue").toString());
        float fltMinValue = Float.parseFloat(intent.getExtras().get("minValue").toString());
        int selectedIndex = Integer.parseInt(intent.getExtras().get("index").toString());

        SeekBarOSCWrapper selectedSeekBar = seekBarOSCWrapperList.get(selectedIndex);
        selectedSeekBar.setMsgValueChanged(msgValueChanged);
        selectedSeekBar.setMaxValue(fltMaxValue);
        selectedSeekBar.setMinValue(fltMinValue);

        saveOSCSettingsIntoFile();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editMenuItem = menu.getItem(1);
        editMenuItem.setTitle(editMode ? "play mode" : "edit mode");

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu:
                toggleEditMode();
                return true;
            case R.id.network_menu:
                createNetworkDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creates WifiAlert Fragment. Called from checkWifiState method
     * on callback, ACTION_WIFI_SETTINGS intent is called to change wifi settings.
     */
    private void createWifiAlertDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(WIFI_ALERT_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        final WifiSettingsDialogFragment frg = WifiSettingsDialogFragment.newInstance();
        frg.setWifiSettingsDialogListener(new WifiSettingsDialogFragment.WifiSettingsDialogListener() {

            public void onWifiSettingsRequested() {
                frg.dismiss();
                try {
                    Intent wifiIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    startActivity(wifiIntent);
                } catch (Exception exp) {
                    Toast.makeText(QuickOSCActivity.this, "Cannot Open Wifi Settings", Toast.LENGTH_SHORT).show();
                }
            }
        });

        frg.show(ft, WIFI_ALERT_DIALOG);
    }


    /***
     * Saves a boolean value in shared preferences.
     * @param tag string to hold the reference to the preference
     * @param value value to be saved
     */
    private void saveSharedPreference(String tag, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE, 0).edit();
        editor.putBoolean(tag, value);
        editor.apply();
    }

    /**
     * Creates Network Settings Fragment. Called from onOptionsItemSelected method
     * onSettingsSaved callback from the fragment stores ipAddress and port values
     * and reconnects to the network
     */
    private void createNetworkDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(NETWORK_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        final NetworkDialogFragment frg = NetworkDialogFragment.newInstance(ipAddress, port, mListenIncoming, Utils.getIpAddress(true), inPort, OSC_START_PATH);
        frg.setNetworkDialogListener(new NetworkDialogFragment.NetworkDialogListener() {
            public void onSettingsSaved(String ipAddress, int port, boolean listenIncoming, int inPort, String startPath) {
                QuickOSCActivity.this.ipAddress = ipAddress;
                QuickOSCActivity.this.port = port;
                QuickOSCActivity.this.inPort = inPort;
                QuickOSCActivity.this.OSC_START_PATH = startPath;

                saveNetworkSettinsIntoFile();
                initializeOSC();
                initializeIncomingOSC();
            }
        });
        frg.show(ft, NETWORK_DIALOG);
    }

    /**
     * Toggles edit mode triggered by the menu option.
     */
    private void toggleEditMode() {
        if (isEditMode()) {
            this.editMode = false;
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            Toast.makeText(this, "Edit Mode Disabled", Toast.LENGTH_SHORT).show();
        } else {
            this.editMode = true;
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            Toast.makeText(this, "Edit Mode Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    /***
     * Initializes the OSCPortOut class with the given ipAddress and port.
     * Called once at the beginning in onCreate() method and at the end of the network settings dialog save action.
     */
    private void initializeOSC() {
        try {

            if (oscPortOut != null) {
                oscPortOut.close();
            }

            oscPortOut = new OSCPortOut(InetAddress.getByName(ipAddress), port);
        } catch (Exception exp) {
            Toast.makeText(this, "Error Initializing OSC", Toast.LENGTH_SHORT).show();
            oscPortOut = null;
        }
    }

    /**
     * Initializes the OSCPortIn class with with the given port
     * called from onStart method
     * <p>
     * AVAILABLE MESSAGE FORMATS
     * <p>
     * for Button controller, N = {1..16}
     * /btnN/msgButtonPressed <args ...>
     * /btnN/msgButtonReleased <args..>
     * /btnN/triggerOnButtonReleased <1/0>
     * <p>
     * for Toggle controllers, N = {1..8}
     * /togN/msgToggledOn <args >
     * /togN/msgToggledOff <args>
     * /togN/value <1/0>
     * <p>
     * for SeekBar controllers, N = {1..4}
     * /sbN/range <min> <max>
     * /sbN/msgValueChanged <args>
     * /sbN/value <val>
     * <p>
     * To save OSC settings
     * /save
     */
    private void initializeIncomingOSC() {
        if (this.oscPortIn != null) {
            if (this.oscPortIn.isListening()) {
                this.oscPortIn.stopListening();
            }

            this.oscPortIn.close();
            this.oscPortIn = null;

        }

        if (!this.mListenIncoming) {
            return;
        }

        try {
            this.oscPortIn = new OSCPortIn(this.inPort);
            this.oscPortIn.addListener("/btn*/*", btnListener);
            this.oscPortIn.addListener("/tog*/*", toggleListener);
            this.oscPortIn.addListener("/sb*/*", seekBarListener);
            this.oscPortIn.addListener("/save", saveListener);
            this.oscPortIn.startListening();
        } catch (SocketException se) {
            se.printStackTrace();
            Log.d("QuickOSCActivity", se.getMessage());
        }
    }

    /**
     * Sends the OSC message passed by the Wrappers. Requires a successful initializeOSC() method
     * to be able to access the host.
     *
     * @param message
     */
    public void sendOSC(String message) {
        try {
            new AsyncSendOSCTask(this, this.oscPortOut).execute(new OSCMessage(message));
        } catch (Exception exp) {
            Toast.makeText(this, "Error Sending Message", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends the OSC message passed by the Wrappers. Requires a successful initializeOSC() method
     * to be able to access the host.
     *
     * @param address
     * @param arguments
     */

    public void sendOSC(String address, List<Object> arguments) {
        address = OSC_START_PATH + address;
        try {
            new AsyncSendOSCTask(this, this.oscPortOut).execute(new OSCMessage(address, arguments));
        } catch (Exception exp) {
            Toast.makeText(this, "Error Sending Message " + address, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Saves network settings to file to be on next startup
     */
    private void saveNetworkSettinsIntoFile() {
        try {
            try {
                FileOutputStream fos = openFileOutput(NETWORK_SETTINGS_FILE, Context.MODE_PRIVATE);

                String data = ipAddress + "#" + port + "#" + mListenIncoming + "#" + inPort + "#" + OSC_START_PATH;
                fos.write(data.getBytes());
                fos.close();
            } catch (Exception exp) {
                Toast.makeText(this, "Could Not Update SCAuth File", Toast.LENGTH_SHORT).show();
                exp.printStackTrace();
            }
        } catch (Exception exp) {
            Toast.makeText(this, "Error Saving Network Settings", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Restores network settings which were saved in previous sessions
     */
    private void restoreNetworkSettingsFromFile() {
        try {
            FileInputStream fis = openFileInput(NETWORK_SETTINGS_FILE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int bytes_read;
            while ((bytes_read = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytes_read);
            }

            String data = new String(baos.toByteArray());
            String[] pieces = data.split("#");

            ipAddress = pieces[0];
            port = Integer.parseInt(pieces[1]);
            if (pieces.length > 2) {
                mListenIncoming = Boolean.parseBoolean(pieces[2]);
                inPort = Integer.parseInt(pieces[3]);
                OSC_START_PATH = pieces[4];
            } else {
                mListenIncoming = false;
                inPort = 8090;
                OSC_START_PATH = "/";
            }
        } catch (FileNotFoundException fnfe) {
        } catch (Exception exp) {
            Toast.makeText(this, "Could Not Read SCAuth File", Toast.LENGTH_SHORT).show();
            ipAddress = "127.0.0.1";
            port = 8000;
            mListenIncoming = false;
            inPort = 8090;
        }
    }

    private void restoreOSCSettingsFromFile() {
        try {
            oscSettingsHashtable.clear();

            FileInputStream fis = openFileInput(OSC_SETTINGS_FILE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int bytes_read;
            while ((bytes_read = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytes_read);
            }

            String data = new String(baos.toByteArray());
            String[] pieces = data.split("#x#x#");

            for (int i = 0; i < pieces.length; i += 2) {
                oscSettingsHashtable.put(pieces[i].trim(), pieces[i + 1].trim());
            }
        } catch (FileNotFoundException fnfe) {
        } catch (Exception exp) {
            Toast.makeText(this, "Could Not Read OSC Settings File", Toast.LENGTH_SHORT).show();
            oscSettingsHashtable.clear();
        }
    }

    /**
     * Saves network settings to file to be on next startup
     */
    private void saveOSCSettingsIntoFile() {
        try {
            try {
                FileOutputStream fos = openFileOutput(OSC_SETTINGS_FILE, Context.MODE_PRIVATE);

                StringBuilder dataBuffer = new StringBuilder();
                for (int i = 0; i < buttonOSCWrapperList.size(); i++) {
                    ButtonOSCWrapper thisButtonWrapper = buttonOSCWrapperList.get(i);
                    dataBuffer.append("btn").append(i + 1).append("-lbl").append("#x#x#").append(thisButtonWrapper.getName()).append("#x#x#");
                    dataBuffer.append("btn").append(i + 1).append("-butpres").append("#x#x#").append(thisButtonWrapper.getMessageButtonPressedRaw()).append("#x#x#");
                    dataBuffer.append("btn").append(i + 1).append("-trgbutrel").append("#x#x#").append(thisButtonWrapper.getTriggerWhenButtonReleased()).append("#x#x#");
                    dataBuffer.append("btn").append(i + 1).append("-butrel").append("#x#x#").append(thisButtonWrapper.getMessageButtonReleasedRaw()).append("#x#x#");
                }

                for (int i = 0; i < toggleOSCWrapperList.size(); i++) {
                    ToggleOSCWrapper thisToggleOSCWrapper = toggleOSCWrapperList.get(i);
                    dataBuffer.append("tog").append(i + 1).append("-onlbl").append("#x#x#").append(thisToggleOSCWrapper.getOnLabel()).append("#x#x#");
                    dataBuffer.append("tog").append(i + 1).append("-offlbl").append("#x#x#").append(thisToggleOSCWrapper.getOffLabel()).append("#x#x#");
                    dataBuffer.append("tog").append(i + 1).append("-togon").append("#x#x#").append(thisToggleOSCWrapper.getMessageToggleOnRaw()).append("#x#x#");
                    dataBuffer.append("tog").append(i + 1).append("-togoff").append("#x#x#").append(thisToggleOSCWrapper.getMessageToggleOffRaw()).append("#x#x#");
                }

                for (int i = 0; i < seekBarOSCWrapperList.size(); i++) {
                    SeekBarOSCWrapper thisSeekBarOSCWrapper = seekBarOSCWrapperList.get(i);
                    dataBuffer.append(thisSeekBarOSCWrapper.getName()).append("-valcng").append("#x#x#").append(thisSeekBarOSCWrapper.getMsgValueChanged()).append("#x#x#");
                    dataBuffer.append(thisSeekBarOSCWrapper.getName()).append("-minval").append("#x#x#").append(thisSeekBarOSCWrapper.getMinValue()).append("#x#x#");
                    dataBuffer.append(thisSeekBarOSCWrapper.getName()).append("-maxval").append("#x#x#").append(thisSeekBarOSCWrapper.getMaxValue()).append("#x#x#");
                }

                String data = dataBuffer.toString();
                data = data.substring(0, data.length() - 5);

                fos.write(data.getBytes());
                fos.close();
            } catch (Exception exp) {
                Toast.makeText(this, "Could Not Update SCAuth File", Toast.LENGTH_SHORT).show();
                exp.printStackTrace();
            }
        } catch (Exception exp) {
            Toast.makeText(this, "Error Saving Network Settings", Toast.LENGTH_SHORT).show();
        }
    }

    private float safeFloatParse(String val, float defVal) {
        try {
            return Float.parseFloat(val);
        } catch (Exception nfe) {
            return defVal;
        }
    }

    @Override
    public void onBackPressed() {
        if (this.editMode) {
            this.editMode = false;
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            Toast.makeText(this, "Edit Mode Disabled", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    private boolean checkWifiState() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            createWifiAlertDialog();
            return false;
        }

        return true;
    }

    /**
     * A utility method to extract the id from the OSC address.
     *
     * @param str    the first part of the original OSC address
     * @param prefix the address starts with either btn, tog or sb
     * @return the number after the prefix, 0 if something is wrong
     */
    private int extractIndex(String str, String prefix) {
        int result;
        try {
            result = Integer.parseInt(str.substring(prefix.length()), 10);
            if (result < 1) {
                return 0;
            }

            return result;
        } catch (NumberFormatException nfe) {
        }
        return 0;
    }

    public List<ButtonOSCWrapper> getButtonWrappers() {
        return this.buttonOSCWrapperList;
    }

    public List<ToggleOSCWrapper> getToggleWrappers() {
        return this.toggleOSCWrapperList;
    }

    public List<SeekBarOSCWrapper> getSeekBarWrappers() {
        return this.seekBarOSCWrapperList;
    }
}