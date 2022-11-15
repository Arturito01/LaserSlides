package cat.merino.albert.laserslides;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * ToggleOSCSettingActivity
 * On this screen, the OSC messages are configured for Toggle controls.
 * msgToggleOn and msgToggleOff intent data are passed from the caller Activity.
 * At the end, values are passed back to the caller with the same parameter names.
 *
 * @author ahmetkizilay
 */
public class ToggleOSCSettingActivity extends Activity implements OnClickListener {

    private EditText editTextOnLabel;
    private EditText editTextOffLabel;
    private EditText editTextToggleOn;
    private EditText editTextToggleOff;

    private Button btnToggleSave;
    private Button btnToggleCancel;

    private int selectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toggle_osc_layout);

        Intent originalIntent = getIntent();
        String onLabel = originalIntent.getStringExtra("onLabel");
        String offLabel = originalIntent.getStringExtra("offLabel");
        String msgToggleOn = originalIntent.getStringExtra("msgToggleOn");
        String msgToggleOff = originalIntent.getStringExtra("msgToggleOff");
        this.selectedIndex = originalIntent.getIntExtra("index", 0);

        editTextOnLabel = (EditText) findViewById(R.id.etOnLabel);
        if (onLabel != null && !onLabel.equalsIgnoreCase("")) {
            editTextOnLabel.setText(onLabel);
        }

        editTextOffLabel = (EditText) findViewById(R.id.etOffLabel);
        if (offLabel != null && !offLabel.equalsIgnoreCase("")) {
            editTextOffLabel.setText(offLabel);
        }

        editTextToggleOn = (EditText) findViewById(R.id.etToggleOn);
        if (msgToggleOn != null && !msgToggleOn.equalsIgnoreCase("")) {
            editTextToggleOn.setText(msgToggleOn);
        }

        editTextToggleOff = (EditText) findViewById(R.id.etToggleOff);
        if (msgToggleOff != null && !msgToggleOff.equalsIgnoreCase("")) {
            editTextToggleOff.setText(msgToggleOff);
        }

        btnToggleSave = (Button) findViewById(R.id.btnToggleSave);
        btnToggleSave.setOnClickListener(this);

        btnToggleCancel = (Button) findViewById(R.id.btnToggleCancel);
        btnToggleCancel.setOnClickListener(this);
    }

    public void onClick(View view) {
        Intent data = new Intent();

        if (view.equals(btnToggleSave)) {
            data.putExtra("onLabel", editTextOnLabel.getText());
            data.putExtra("offLabel", editTextOffLabel.getText());
            data.putExtra("msgToggleOn", editTextToggleOn.getText());
            data.putExtra("msgToggleOff", editTextToggleOff.getText());
            data.putExtra("index", this.selectedIndex);

            setResult(Activity.RESULT_OK, data);
        } else if (view.equals(btnToggleCancel)) {
            setResult(Activity.RESULT_CANCELED, data);
        } else {
            // you should not be here
            data.putExtra("errorMessage", "weird error");
            setResult(Activity.RESULT_CANCELED, data);
        }

        finish();
    }
}
