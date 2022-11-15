package cat.merino.albert.laserslides.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import cat.merino.albert.laserslides.R;


/**
 * A DialogFragment class to display the Wifi Not Detected alert.
 * Defines an interface with one method to request wifi settings page
 * Created by ahmetkizilay on 17.07.2014.
 */
public class WifiSettingsDialogFragment extends DialogFragment {
    public WifiSettingsDialogListener mCallback;

    public static WifiSettingsDialogFragment newInstance() {
        WifiSettingsDialogFragment frg = new WifiSettingsDialogFragment();
        Bundle args = new Bundle();

        frg.setArguments(args);
        return frg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
        alert.setTitle("Wifi Not Detected");
        alert.setIcon(R.drawable.ic_launcher_background);
        alert.setCancelable(false);
        alert.setMessage("Enable Wifi For OSC");
        alert.setButton(Dialog.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        alert.setButton(Dialog.BUTTON_POSITIVE, "Wifi Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onWifiSettingsRequested();
                }
            }
        });
        return alert;
    }

    public void setWifiSettingsDialogListener(WifiSettingsDialogListener callback) {
        this.mCallback = callback;
    }

    public interface WifiSettingsDialogListener {
        public void onWifiSettingsRequested();
    }

}
