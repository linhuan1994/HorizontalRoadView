package lgx.horizontalview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    HorizontalRoadView mroadView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       mroadView= (HorizontalRoadView) findViewById(R.id.mroadView);//实例化
        Button button = (Button) findViewById(R.id.nextLocation);
        button.setOnClickListener(this);
        Button mbutton= (Button) findViewById(R.id.nextCar);
        mbutton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.nextLocation://设置人的下一个位置
               mroadView.setMyCurrentLocation(mroadView.getMyCurrentLocation()+1);
                Toast.makeText(MainActivity.this,"下一个位置 ",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nextCar://设置车下一个位置
               mroadView.setCarLocation(mroadView.getCarLocation()+1);
                Toast.makeText(MainActivity.this,"下一个车站",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
