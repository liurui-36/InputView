package com.qiqi.inputview;

/*import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String tag = getClass().getSimpleName();
    private InputView inputView;
    private Button btnUnder;
    private Button btnRect;
    private Button btnFill;
    private Button btnText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputView = (InputView) findViewById(R.id.passwordView);
        btnUnder = (Button) findViewById(R.id.btn_under);
        btnUnder.setOnClickListener(this);
        btnRect = (Button) findViewById(R.id.btn_rect);
        btnRect.setOnClickListener(this);
        btnFill = (Button) findViewById(R.id.btn_fill);
        btnFill.setOnClickListener(this);
        btnText = (Button) findViewById(R.id.btn_text);
        btnText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_under:
                inputView.setMode(InputView.Mode.UNDERLINE);
                break;
            case R.id.btn_rect:
                inputView.setMode(InputView.Mode.RECT);
                break;
            case R.id.btn_fill:
                inputView.setMode(InputView.Mode.FILL);
                break;
            case R.id.btn_text:
                Toast.makeText(MainActivity.this, inputView.getText(), Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}*/
