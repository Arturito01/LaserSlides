package cat.merino.albert.laserslides.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import cat.merino.albert.laserslides.R;


/**
 * Created by ahmetkizilay on 17.07.2014.
 */
public class NetworkDialogFragment extends DialogFragment {
    private String mIpAddress;
    private int mPort;
    private boolean mListenIncoming;
    private String mDeviceIp;
    private int mInPort;
    private String mStartPath;
    private NetworkDialogListener mCallback;

    public static NetworkDialogFragment newInstance(String ipAddress, int port, boolean listenIncoming, String deviceIpAddress, int inPort, String startPath) {
        NetworkDialogFragment frg = new NetworkDialogFragment();
        Bundle args = new Bundle();
        args.putString("ipAddress", ipAddress);
        args.putInt("port", port);
        args.putBoolean("listenIncoming", listenIncoming);
        args.putString("deviceIp", deviceIpAddress);
        args.putInt("inPort", inPort);
        args.putString("startPath", startPath);
        frg.setArguments(args);
        return frg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        this.mIpAddress = (String) args.get("ipAddress");
        this.mPort = args.getInt("port");
        this.mListenIncoming = args.getBoolean("listenIncoming");
        this.mDeviceIp = (String) args.get("deviceIp");
        this.mInPort = args.getInt("inPort");
        this.mStartPath = args.getString("startPath");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View networkView = inflater.inflate(R.layout.dialog_network_settings, null);
        EditText etNetworkIP = (EditText) networkView.findViewById(R.id.etNetworkIP);
        etNetworkIP.setText(this.mIpAddress);

        EditText etNetworkPort = (EditText) networkView.findViewById(R.id.etNetworkPort);
        etNetworkPort.setText(Integer.toString(mPort));

        final EditText etDeviceIp = (EditText) networkView.findViewById(R.id.etDeviceIP);
        etDeviceIp.setText(this.mDeviceIp);

        EditText etNetworkInPort = (EditText) networkView.findViewById(R.id.etNetworkInPort);
        etNetworkInPort.setText(Integer.toString(this.mInPort));

        EditText etStartPath = (EditText) networkView.findViewById(R.id.etStartPath);
        etStartPath.setText(this.mStartPath);


        CheckBox cbListenIncoming = (CheckBox) networkView.findViewById(R.id.cbListenIncoming);
        cbListenIncoming.setChecked(this.mListenIncoming);

        final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
        alert.setView(networkView);
        alert.setTitle("Network Settings");
        alert.setButton(Dialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText etNetworkIP = (EditText) alert.findViewById(R.id.etNetworkIP);
                String newIpAddress = etNetworkIP.getText().toString();

                EditText etStartPath = (EditText) alert.findViewById(R.id.etStartPath);
                String startPath = etStartPath.getText().toString();

                EditText etNetworkPort = (EditText) alert.findViewById(R.id.etNetworkPort);
                int newPort = Integer.parseInt(etNetworkPort.getText().toString());

                CheckBox cbListenIncoming = (CheckBox) alert.findViewById(R.id.cbListenIncoming);
                boolean listenIncoming = cbListenIncoming.isChecked();

                EditText etInPort = (EditText) alert.findViewById(R.id.etNetworkInPort);
                int newInPort = Integer.parseInt(etInPort.getText().toString());


                if (mCallback != null) {
                    mCallback.onSettingsSaved(newIpAddress, newPort, listenIncoming, newInPort, startPath);
                }
            }
        });
        return alert;
    }

    public void setNetworkDialogListener(NetworkDialogListener callback) {
        this.mCallback = callback;
    }

    public interface NetworkDialogListener {
        public void onSettingsSaved(String ipAddress, int port, boolean listenIncoming, int incomingPort, String startPath);
    }
}
