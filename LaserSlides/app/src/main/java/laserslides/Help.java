package LaserSlides;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;

public class Help extends AppCompatActivity implements OnClickListener{
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_button);

        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.back){
            Intent quickOSC = new Intent(Help.this, QuickOSCActivity.class);
            startActivity(quickOSC);
        }
    }
}