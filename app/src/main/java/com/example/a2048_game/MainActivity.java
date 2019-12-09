package com.example.a2048_game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2048_game.db.Helper;
import com.example.a2048_game.db.Operator;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements View.OnTouchListener {


    private LinearLayout layout;
    private boolean gameOver = true;   //游戏未开始，或已结束
    private Button startGame;

    private TextView textScore;
    private TextView textBestScore;
    private TextView textGameover;
    private static int score = 0;
    private static int bestScore = 0;
    private MediaPlayer mediaPlayer = null;
    private String start = "/data/data/com.example.a2048_game/file/music2.mp3";
    private String move = "/data/data/com.example.a2048_game/file/music1.mp3";
    private String end = "/data/data/com.example.a2048_game/file/music2.mp3";

    //触摸事件手指按下和松开的两个坐标
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    //16个方块的id
    private static int[][] btnBlock = {
            {R.id.btn00, R.id.btn01, R.id.btn02, R.id.btn03},
            {R.id.btn10, R.id.btn11, R.id.btn12, R.id.btn13},
            {R.id.btn20, R.id.btn21, R.id.btn22, R.id.btn23},
            {R.id.btn30, R.id.btn31, R.id.btn32, R.id.btn33}
    };

    //16个方块的对应的值
    private static int[][] flag = {
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
    };

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_VOICEMAIL};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);   //去掉标题
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   //锁定竖屏
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);

        layout = findViewById(R.id.main_layout);//整个页面
        layout.setOnTouchListener(this);// 获得滑屏方向
        textScore = (TextView) findViewById(R.id.score);
        textBestScore = (TextView) findViewById(R.id.best_score);
        textGameover = (TextView) findViewById(R.id.game_over);
        startGame = (Button) findViewById(R.id.start_game);

        try{
            //获取最高分
            Helper helper = new Helper(this);
            Operator operator = new Operator(this, helper);
            String str = operator.getBestScore();
            textBestScore.setText("最高分数\n" + str);
        }
        catch(Exception e){
            textBestScore.setText("最高分数\n2");
        }

        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isGameOver()) {
                    startGame.setText("重新开始");
                    initMediaPlayer(start);
                    initView();
                    getNext();
                    getNext();
                }
            }
        });


    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * 初始化布局、分数等
     */
    public void initView() {
        TextView view;
        score = 0;
        textScore.setText("当前分数\n0");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                flag[i][j] = 0;//数组初始化为0  视图初始化
                view = (TextView) findViewById(btnBlock[i][j]);
                view.setText("");
                view.setBackground(getDrawable(R.drawable.block_init));
            }
        }
    }


    public void onClick(View view) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!gameOver) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = motionEvent.getRawX();    //获得 开始触摸点到屏幕最左侧的距离
                    y1 = motionEvent.getRawY();     //获得 开始触摸点到屏幕最上侧的距离
                    break;
                case MotionEvent.ACTION_MOVE:
                    x2 = motionEvent.getRawX();     //获得 移动时触摸点到屏幕最左侧的距离
                    y2 = motionEvent.getRawY();     //获得 移动时触摸点到屏幕最上侧的距离
                    break;
                case MotionEvent.ACTION_UP:   //离开屏幕时，判断移动方向并处理结果
                    //为防止误判，认为水平方向 移动距离 较大时：为左右移动
                    // 垂直方向移动距离大时： 为上下移动
                    //并 认为移动距离大于100时为滑动操作
                    if (Math.abs(x2 - x1) < Math.abs(y2 - y1) && y1 - y2 > 200) {   //上滑
                        initMediaPlayer(move);
                        moveToUp();
                        textScore.setText("当前分数\n" + updateScore());
                        updateBlock();
                        //更新方块
                        if (!isGameOver()) {
                            getNext();
                        }
                    } else if (Math.abs(x2 - x1) < Math.abs(y2 - y1) && y2 - y1 > 200) {       //下滑
                        initMediaPlayer(move);
                        moveToDown();
                        textScore.setText("当前分数\n" + updateScore());
                        updateBlock();
                        //更新方块
                        if (!isGameOver()) {
                            getNext();
                        }
                    } else if (x1 - x2 > 200 && Math.abs(x2 - x1) > Math.abs(y2 - y1)) {    //左滑
                        initMediaPlayer(move);
                        moveToLeft();
                        textScore.setText("当前分数\n" + updateScore());
                        updateBlock();
                        //更新方块
                        if (!isGameOver()) {
                            getNext();
                        }
                    } else if (x2 - x1 > 200 && Math.abs(x2 - x1) > Math.abs(y2 - y1)) {//右滑
                        initMediaPlayer(move);
                        moveToRight();
                        textScore.setText("当前分数\n" + updateScore());
                        updateBlock();
                        //更新方块
                        if (!isGameOver()) {
                            getNext();
                        }
                    }
            }
        }
        return true;
    }

    /**
     * 随机取设置为2 的方块
     * 0-16的随机数除以4，商为 行, 余数为 列
     */
    public void getNext() {
        if (!isGameOver()) { //游戏未结束
            int next = getRandom();
            while (flag[next / 4][next % 4] != 0) {
                next = getRandom();
            }
            flag[next / 4][next % 4] = 2;
            TextView textView = (TextView) findViewById(btnBlock[next / 4][next % 4]);
            textView.setTextSize(28);
            textView.setBackground(getDrawable(R.drawable.block_2));
            textView.setText("2");
        } else {
            Toast.makeText(MainActivity.this, "GAME OVER", Toast.LENGTH_SHORT);
        }

    }

    //获得随机数
    public int getRandom() {
        Random r = new Random();
        return r.nextInt(16);
    }


    //判断游戏是否结束
    public boolean isGameOver() {
        gameOver = true;  //每次初始化为true
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (flag[i][j] == 0) {       //存在为0 的坐标说明游戏未结束
                    gameOver = false;
                    break;
                }
            }
        }
        if (gameOver) {
            //保存分数 初始化方块
            textGameover.setText("上次游戏分数为\n" + score);
            Helper helper = new Helper(this);
            Operator operator = new Operator(this, helper);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            String time =df.format(new Date());
            operator.insertScore(String.valueOf(score),time);
            //刷新最高分
            String str = operator.getBestScore();
            textBestScore.setText("最高分数\n" + str);
            initMediaPlayer(end);
            Toast.makeText(this, "GAME OVER", Toast.LENGTH_SHORT).show();
            initView();
        }
        return gameOver;
    }

    /**
     * 播放歌曲
     */
    public void initMediaPlayer(String path) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        //播放内存卡中音频文件
        mediaPlayer = new MediaPlayer();
        //设置音源
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }


    /**
     * 上滑
     */
    public void moveToUp() {
        for (int j = 0; j < 4; j++) {   //逐列处理
            ArrayList<Integer> list = new ArrayList<>();
            ArrayList<Integer> temp = new ArrayList<>();
            //先找到不为 0 的数
            for (int i = 0; i < 4; i++) {
                if (flag[i][j] != 0) {
                    list.add(flag[i][j]);
                }
            }
            int i = 0;
            //相邻相同的数相加
            for (i = 0; i < list.size() - 1; i++) {
                if (list.get(i) == 128 || list.get(i) == 64) {
                    Log.i("log128", list.get(i) + "");
                }
                if (list.get(i).equals(list.get(i + 1))) {
                    temp.add(list.get(i) * 2);
                    i++;
                } else {
                    temp.add(list.get(i));
                }
            }
            //说明最后两数不相等，则将最后一个数也存到temp
            if (i == list.size() - 1) {
                temp.add(list.get(i));
            }
            //将处理结果赋值给flag， 从上到下先赋值不为0的数
            for (i = 0; i < temp.size(); i++) {
                flag[i][j] = temp.get(i);
            }
            for (i = temp.size(); i < 4; i++) {
                flag[i][j] = 0;
            }
        }
    }

    /**
     * 下滑
     */
    public void moveToDown() {
        for (int j = 0; j < 4; j++) {
            ArrayList<Integer> list = new ArrayList<>();
            ArrayList<Integer> temp = new ArrayList<>();
            //从下向上 找不为 0 的数
            for (int i = 0; i < 4; i++) {
                if (flag[3 - i][j] != 0) {
                    list.add(flag[3 - i][j]);
                }
            }
            //将相邻相同的数相加
            int i = 0;
            for (i = 0; i < list.size() - 1; i++) {
                if (list.get(i).equals(list.get(i + 1))) {
                    temp.add(list.get(i) * 2);
                    i++;
                } else {
                    temp.add(list.get(i));
                }
            }
            //说明最后两数不相等，将最后一个数也保存到temp
            if (i == list.size() - 1) {
                temp.add(list.get(i));
            }
            //从下向上 将处理结果赋值 给flag ，先赋值不为 0 的数，其余的为0
            for (i = 0; i < temp.size(); i++) {
                flag[3 - i][j] = temp.get(i);
            }
            for (i = temp.size(); i < 4; i++) {
                flag[3 - i][j] = 0;
            }
        }
    }

    /**
     * 右滑
     */
    public void moveToRight() {
        for (int j = 0; j < 4; j++) {
            ArrayList<Integer> list = new ArrayList<>();
            ArrayList<Integer> temp = new ArrayList<>();
            //先找到该行不为 0 的数 存到list里
            for (int i = 0; i < 4; i++) {
                if (flag[j][3 - i] != 0) {       //从右向左找，方便修改flag的值
                    list.add(flag[j][3 - i]);
                }
            }
            //遍历list 将邻近相等的两数相加， 放到新的list里
            int i = 0;
            for (i = 0; i < list.size() - 1; i++) {
                if (list.get(i).equals(list.get(i + 1))) {
                    temp.add(list.get(i) + list.get(i));
                    i++;
                } else {
                    temp.add(list.get(i));
                }
            }
            //说明最后两个数不相等,则将最后一个数加入list
            if (i == list.size() - 1) {
                temp.add(list.get(i));
            }

            //从右开始给flag赋值，先将不为0的复制给flag的该行
            //剩余的则为0
            for (i = 0; i < temp.size(); i++) {
                flag[j][3 - i] = temp.get(i);
            }
            for (i = temp.size(); i < 4; i++) {
                flag[j][3 - i] = 0;
            }
        }
    }


    /**
     * 向左滑动
     */
    public void moveToLeft() {
        for (int i = 0; i < 4; i++) {
            ArrayList<Integer> list = new ArrayList<>();
            ArrayList<Integer> temp = new ArrayList<>();
            //找到该行不为 0 的数
            for (int j = 0; j < 4; j++) {
                if (flag[i][j] != 0) {     //从左向右找
                    list.add(flag[i][j]);
                }
            }
            int p = 0;
            //相邻相同的两数相加
            for (p = 0; p < list.size() - 1; p++) {
                if (list.get(p).equals(list.get(p + 1))) {
                    temp.add(list.get(p) * 2);
                    p++;
                } else {
                    temp.add(list.get(p));
                }
            }
            //说明最后两个数不相等,则将最后一个数加入list
            if (p == list.size() - 1) {
                temp.add(list.get(p));
            }
            //将处理过的结果按从左到右赋给flag
            for (p = 0; p < temp.size(); p++) {
                flag[i][p] = temp.get(p);
            }
            for (p = temp.size(); p < 4; p++) {
                flag[i][p] = 0;
            }
        }
    }


    /**
     * 更新方块视图
     */
    public void updateBlock() {
        TextView view;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                view = (TextView) findViewById(btnBlock[i][j]);
                switch (flag[i][j]) {
                    case 0:
                        view.setText("");
                        view.setBackground(getDrawable(R.drawable.block_init));
                        break;
                    case 2:
                        view.setTextSize(28);
                        view.setBackground(getDrawable(R.drawable.block_2));
                        view.setText("2");
                        break;
                    case 4:
                        view.setText("4");
                        view.setTextSize(28);
                        view.setBackground(getDrawable(R.drawable.block_4));
                        break;
                    case 8:
                        view.setText("8");
                        view.setTextSize(28);
                        view.setBackground(getDrawable(R.drawable.block_8));
                        break;
                    case 16:
                        view.setText("16");
                        view.setTextSize(25);
                        view.setBackground(getDrawable(R.drawable.block_16));
                        break;
                    case 32:
                        view.setText("32");
                        view.setTextSize(25);
                        view.setBackground(getDrawable(R.drawable.block_32));
                        break;
                    case 64:
                        view.setText("64");
                        view.setTextSize(25);
                        view.setBackground(getDrawable(R.drawable.block_64));
                        break;
                    case 128:
                        view.setText("128");
                        view.setTextSize(20);
                        view.setBackground(getDrawable(R.drawable.block_128));
                        break;
                    case 256:
                        view.setText("256");
                        view.setTextSize(20);
                        view.setBackground(getDrawable(R.drawable.block_256));
                        break;
                    case 512:
                        view.setText("512");
                        view.setTextSize(20);
                        view.setBackground(getDrawable(R.drawable.block_512));
                        break;
                    case 1024:
                        view.setText("1024");
                        view.setTextSize(18);
                        view.setBackground(getDrawable(R.drawable.block_1024));
                        break;
                    case 2048:
                        view.setBackground(getDrawable(R.drawable.block_2048));
                        view.setText("2048");
                        view.setTextSize(18);
                        break;
                    default:
                        view.setText("");
                        view.setTextSize(28);
                        view.setBackground(getDrawable(R.drawable.block_init));
                        break;
                }
            }
        }
    }

    /**
     * 更新当前分数
     */
    public int updateScore() {

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (flag[i][j] > score) {
                    score = flag[i][j];
                }
            }
        }
        return score;
    }

}