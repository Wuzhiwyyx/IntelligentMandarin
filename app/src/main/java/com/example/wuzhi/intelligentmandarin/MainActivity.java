package com.example.wuzhi.intelligentmandarin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuzhi.intelligentmandarin.DataClass.LearnedSentence;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedVocabulary;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedWord;
import com.example.wuzhi.intelligentmandarin.DataClass.Sentence;
import com.example.wuzhi.intelligentmandarin.DataClass.Vocabulary;
import com.example.wuzhi.intelligentmandarin.DataClass.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer_layout;
    private Button startDailyExercise;
    private TextView days, todayCount, scale;
    private ProgressBar progressBar;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public static int wordNum;
    public static int vocabularyNum;
    public static int sentenceNum;
    public static int wordPerDay = 5;
    public static int vocabularyPerDay = 5;
    public static int sentencePerDay = 5;
    public static int requestCount = 1;
    public static String voicer;

    public static List<Word> todayWords = new ArrayList<>();
    public static List<Vocabulary> todayVocabularies = new ArrayList<>();
    public static List<Sentence> todaySentences = new ArrayList<>();

    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;

    private LineChartView lineChart;
//    String[] date = {"3-31", "4-1", "4-2", "4-3", "4-4"};//X轴的标注
    List<String> dates = new ArrayList<>();
//    float[] score = {5.0f, 4.2f, 4.0f, 3.3f, 4.1f};//图表的数据点
    List<Double> scores = new ArrayList<>();
    private List<PointValue> mPointValues = new ArrayList<>();
    private List<AxisValue> mAxisXValues = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOriginalView();
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=583c506b");
//        Button button = (Button) findViewById(R.id.connectTest);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showNotification((new Random()).nextInt(10), "sdfsdf", "dfsfsd");
//            }
//        });
    }

    private class ViewPagerAdapter extends PagerAdapter {
        private List<View> viewList;
        public ViewPagerAdapter(List<View> viewList) {
            this.viewList = viewList;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }
    }

    public void initOriginalView() {
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawer_layout.closeDrawers();
                Intent toWordVocabulary = new Intent(MainActivity.this, TrainWordVocabularyActivity.class);
                Intent toSentenceParagraph = new Intent(MainActivity.this, TrainSentenceParagraphActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_word:
                        toWordVocabulary.putExtra("category", "read_syllable");
                        toWordVocabulary.putExtra("title", item.getTitle());
                        toWordVocabulary.putExtra("isSelf", false);
                        startActivityForResult(toWordVocabulary, 1);
                        break;
//                    case R.id.nav_vocabulary:
//                        toWordVocabulary.putExtra("category", "read_word");
//                        toWordVocabulary.putExtra("title", item.getTitle());
//                        toWordVocabulary.putExtra("isSelf", false);
//                        startActivityForResult(toWordVocabulary, 2);
//                        break;
                    case R.id.nav_sentence:
                        toSentenceParagraph.putExtra("category", "read_sentence");
                        toSentenceParagraph.putExtra("title", item.getTitle());
                        toSentenceParagraph.putExtra("isParagraph", false);
                        toSentenceParagraph.putExtra("isSelf", false);
                        startActivityForResult(toSentenceParagraph, 3);
                        break;
//                    case R.id.nav_paragraph:
//                        toSentenceParagraph.putExtra("category", "read_sentence");
//                        toSentenceParagraph.putExtra("title", item.getTitle());
//                        toSentenceParagraph.putExtra("isParagraph", true);
//                        toSentenceParagraph.putExtra("isSelf", false);
//                        startActivityForResult(toSentenceParagraph, 4);
//                        break;
                    case R.id.nav_dialogue:
                        Intent dIntent = new Intent(MainActivity.this, DialogueActivity.class);
                        startActivityForResult(dIntent, 5);
                        break;
                }
                return true;
            }
        });
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        CircleImageView icon_image = (CircleImageView) headerView.findViewById(R.id.icon_image);
        final EditText usernameEdit = (EditText) headerView.findViewById(R.id.username);
        usernameEdit.setText(pref.getString("username", ""));
        icon_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEdit.getText().toString().trim();
                if (username.equals("")) {
                    Toast.makeText(MainActivity.this, "请输入您的手机号码", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        drawer_layout.closeDrawers();
                                        editor.putString("username", username);
                                        editor.apply();
                                    }
                                });
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url("http://45.32.55.34:8080/mandarin/getword?username=" + username + "&count=" + String.valueOf(requestCount)).build();
                                Response response = client.newCall(request).execute();
                                String responseData = response.body().string();
                                Gson gson = new Gson();
                                List<Word> wordList = gson.fromJson(responseData, new TypeToken<List<Word>>(){}.getType());
                                Log.d("request list.size", String.valueOf(wordList.size()));
                                for (Word w:wordList) {Log.d("request inloop", "true");
                                    w.save();
                                }
                                Request vRequest = new Request.Builder().url("http://45.32.55.34:8080/mandarin/getvocabulary?username=" + username + "&count=" + String.valueOf(requestCount)).build();
                                Response vResponse = client.newCall(vRequest).execute();
                                String vResponseData = vResponse.body().string();
                                List<Vocabulary> vocabularyList = gson.fromJson(vResponseData, new TypeToken<List<Vocabulary>>(){}.getType());
                                Log.d("request list.size", String.valueOf(vocabularyList.size()));
                                for (Vocabulary v:vocabularyList) {Log.d("request inloop", "true");
                                    v.save();
                                }
                                Request sRequest = new Request.Builder().url("http://45.32.55.34:8080/mandarin/getsentence?username=" + username + "&count="  + String.valueOf(requestCount)).build();
                                Response sResponse = client.newCall(sRequest).execute();
                                String sResponseData = sResponse.body().string();
                                List<Sentence> sentenceList = gson.fromJson(sResponseData, new TypeToken<List<Sentence>>(){}.getType());
                                Log.d("request list.size", String.valueOf(sentenceList.size()));
                                for (Sentence s:sentenceList) {Log.d("request inloop", "true");
                                    s.save();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        int total = DataSupport.findAll(Word.class).size() + DataSupport.findAll(Vocabulary.class).size() + DataSupport.findAll(Sentence.class).size();
                                        int learned = DataSupport.findAll(LearnedWord.class).size() + DataSupport.findAll(LearnedVocabulary.class).size() + DataSupport.findAll(LearnedSentence.class).size();
                                        scale.setText(String.valueOf(learned) + "/" + String.valueOf(total));
                                        showNotification((new Random()).nextInt(10),"登录", "数据更新成功");
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bm1:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.bm2:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.bm3:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        List<View> pageList = new ArrayList<>();
        pageList.add(getPageMain());
        pageList.add(getPageStatistics());
        pageList.add(getPageExclusive());
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(pageList);
        viewPager.setAdapter(viewPagerAdapter);
    }

    public View getPageMain() {
        View pageMain = getLayoutInflater().inflate(R.layout.page_main, null);
        startDailyExercise = (Button) pageMain.findViewById(R.id.startDailyExercise);
        startDailyExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (wordNum < wordPerDay) {
                    intent.setClass(MainActivity.this, DailyWordVocabularyActivity.class);
                    intent.putExtra("category", "read_syllable");
                    intent.putExtra("isSelf", false);
                    intent.putExtra("wordNum", wordNum);
                    intent.putExtra("vocabularyNum", vocabularyNum);
                    intent.putExtra("sentenceNum", sentenceNum);
                    startActivityForResult(intent, 2);
                } else if (vocabularyNum < vocabularyPerDay) {
                    intent.setClass(MainActivity.this, DailyWordVocabularyActivity.class);
                    intent.putExtra("category", "read_word");
                    intent.putExtra("isSelf", false);
                    intent.putExtra("wordNum", wordNum);
                    intent.putExtra("vocabularyNum", vocabularyNum);
                    intent.putExtra("sentenceNum", sentenceNum);
                    startActivityForResult(intent, 4);
                } else if (sentenceNum < sentencePerDay){
                    intent.setClass(MainActivity.this, DailySentenceParagraphActivity.class);
                    intent.putExtra("category", "read_sentence");
                    intent.putExtra("isSelf", false);
                    intent.putExtra("wordNum", wordNum);
                    intent.putExtra("vocabularyNum", vocabularyNum);
                    intent.putExtra("sentenceNum", sentenceNum);
                    startActivityForResult(intent, 6);
                } else {
                    Toast.makeText(MainActivity.this, "暂时无新数据", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (DataSupport.findAll(Word.class).isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateData();
                        }
                    });
                }
            }).start();
        }

        days = (TextView) pageMain.findViewById(R.id.days);
        todayCount = (TextView) pageMain.findViewById(R.id.todayCount);
        scale = (TextView) pageMain.findViewById(R.id.scale);
        progressBar = (ProgressBar) pageMain.findViewById(R.id.progressBar);


        updateData();
        return pageMain;
    }

    public View getPageStatistics() {
        View pageStatistics = getLayoutInflater().inflate(R.layout.page_statistics, null);
        lineChart = (LineChartView) pageStatistics.findViewById(R.id.line_chart);
        TextView wSum = (TextView) pageStatistics.findViewById(R.id.wSum);
        TextView vSum = (TextView) pageStatistics.findViewById(R.id.vSum);
        TextView sSum = (TextView) pageStatistics.findViewById(R.id.sSum);

        wSum.setText(String.valueOf(DataSupport.findAll(LearnedWord.class).size()));
        vSum.setText(String.valueOf(DataSupport.findAll(LearnedVocabulary.class).size()));
        sSum.setText(String.valueOf(DataSupport.findAll(LearnedSentence.class).size()));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        long lowerBound = calendar.getTime().getTime();
        calendar.add(calendar.DATE,1);
        long upperBound = calendar.getTime().getTime();
        long step = 60 * 60 * 24 * 1000;

        for (int i = 0; i < 5; i++) {
            double ww = DataSupport.where("lastAccess >= ? and lastAccess < ?", String.valueOf(lowerBound), String.valueOf(upperBound)).average(LearnedWord.class, "score");
            double wv = DataSupport.where("lastAccess >= ? and lastAccess < ?", String.valueOf(lowerBound), String.valueOf(upperBound)).average(LearnedVocabulary.class, "score");
            double ws = DataSupport.where("lastAccess >= ? and lastAccess < ?", String.valueOf(lowerBound), String.valueOf(upperBound)).average(LearnedSentence.class, "score");
            scores.add(ww*0.16 + wv*0.32 + ws*0.48);
            dates.add(sdf.format(new Date(lowerBound)));
            upperBound -= step;
            lowerBound -= step;
        }
        //获取x轴的标注
        for (int i = 0; i < dates.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(dates.get(i)));
        }
        //获取坐标点
        for (int i = 0; i < scores.size(); i++) {
            mPointValues.add(new PointValue(i, scores.get(i).floatValue()));
        }
        initLineChart();//初始化
        return pageStatistics;
    }

    public View getPageExclusive() {
        View pageExclusive = getLayoutInflater().inflate(R.layout.page_exclusive, null);
        SeekBar wPerDay = (SeekBar) pageExclusive.findViewById(R.id.wPerDay);
        SeekBar vPerDay = (SeekBar) pageExclusive.findViewById(R.id.vPerDay);
        SeekBar sPerDay = (SeekBar) pageExclusive.findViewById(R.id.sPerDay);
        SeekBar requestCountBar = (SeekBar) pageExclusive.findViewById(R.id.requestCount);
        final TextView text_wPerDay = (TextView) pageExclusive.findViewById(R.id.text_wPerDay);
        final TextView text_vPerDay = (TextView) pageExclusive.findViewById(R.id.text_vPerDay);
        final TextView text_sPerDay = (TextView) pageExclusive.findViewById(R.id.text_sPerDay);
        final TextView requestCountText = (TextView) pageExclusive.findViewById(R.id.requestCountText);
        wPerDay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_wPerDay.setText(String.valueOf(progress + 1));
                wordPerDay = progress + 1;
                editor.putInt("wordPerDay", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        vPerDay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_vPerDay.setText(String.valueOf(progress + 1));
                vocabularyPerDay = progress + 1;
                editor.putInt("vocabularyPerDay", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sPerDay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_sPerDay.setText(String.valueOf(progress + 1));
                sentencePerDay = progress + 1;
                editor.putInt("sentencePerDay", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        requestCountBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                requestCountText.setText(String.valueOf(progress + 1));
                requestCount = progress + 1;
                editor.putInt("requestCount", requestCount);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        wordPerDay = pref.getInt("wordPerDay", wordPerDay - 1) + 1;
        wPerDay.setProgress(pref.getInt("wordPerDay", wordPerDay - 1));
        text_wPerDay.setText(String.valueOf(pref.getInt("wordPerDay", wordPerDay - 1) + 1));
        vocabularyPerDay = pref.getInt("vocabularyPerDay", vocabularyPerDay - 1) + 1;
        vPerDay.setProgress(pref.getInt("vocabularyPerDay", vocabularyPerDay - 1));
        text_vPerDay.setText(String.valueOf(pref.getInt("vocabularyPerDay", vocabularyPerDay - 1) + 1));
        sentencePerDay = pref.getInt("sentencePerDay", sentencePerDay - 1) + 1;
        sPerDay.setProgress(pref.getInt("sentencePerDay", sentencePerDay - 1));
        text_sPerDay.setText(String.valueOf(pref.getInt("sentencePerDay", sentencePerDay - 1) + 1));
        requestCount = pref.getInt("requestCount", requestCount - 1) + 1;
        requestCountBar.setProgress(pref.getInt("requestCount", requestCount - 1));
        requestCountText.setText(String.valueOf(pref.getInt("requestCount", requestCount - 1) + 1));
        RadioGroup voiceGroup = (RadioGroup) pageExclusive.findViewById(R.id.voiceGroup);
        voicer = pref.getString("voicer", "xiaoyan");
        if (voicer.equals("xiaoyan")) {
            voiceGroup.check(R.id.voiceFemale);
        } else {
            voiceGroup.check(R.id.voiceMale);
        }
        voiceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.voiceMale:
                        voicer = "xiaoyu";
                        break;
                    case R.id.voiceFemale:
                        voicer = "xiaoyan";
                        break;
                }
                editor.putString("voicer", voicer);
                editor.apply();
                Toast.makeText(MainActivity.this, voicer, Toast.LENGTH_SHORT).show();
            }
        });
        return pageExclusive;
    }

    private void initLineChart(){
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(false);//曲线的数据坐标是否加上备注
//        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
//        axisX.setTextColor(Color.BLACK);  //设置字体颜色
//        axisX.setName("date");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(5); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边

//        axisY.setMaxLabelChars(6);//max label length, for example 60
//        axisY.setTextSize(10);//设置字体大小
//        List<AxisValue> values = new ArrayList<>();
//        for(int i = 0; i < 6; i++){
//            AxisValue value = new AxisValue(i);
//            String label = "";
//            value.setLabel(label);
//            values.add(value);
//        }
//        axisY.setValues(values);
//        data.setAxisYLeft(axisY);


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
//        Viewport v = new Viewport(lineChart.getMaximumViewport());
//        v.left = 0;
//        v.right= 7;
//        lineChart.setCurrentViewport(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer_layout.openDrawer(GravityCompat.START);
                break;
        }
        return false;
    }

    public void initData() {
        Word w0 = new Word();
        w0.setWord("发");
        w0.setPronounce("fa");
        w0.setTone("1");
        w0.setProperty("[动]");
        w0.setExample("八国联军发动了侵华战争");
        w0.save();

        Word w1 = new Word();
        w1.setWord("卡");
        w1.setPronounce("ka");
        w1.setTone("3");
        w1.setProperty("[名]");
        w1.setExample("朋友送给我一张卡片");
        w1.save();

        Word w2 = new Word();
        w2.setWord("值");
        w2.setPronounce("zhi");
        w2.setTone("2");
        w2.setProperty("[名]");
        w2.setExample("为了有意义的事情而做出牺牲是值得的");
        w2.save();

        Word w3 = new Word();
        w3.setWord("大");
        w3.setPronounce("da");
        w3.setTone("4");
        w3.setProperty("[形]");
        w3.setExample("今晚的月亮大的像个玉盘");
        w3.save();

        Word w4 = new Word();
        w4.setWord("喝");
        w4.setPronounce("he");
        w4.setTone("1");
        w4.setProperty("[动]");
        w4.setExample("他喝了一口水");
        w4.save();

        Word w5 = new Word();
        w5.setWord("侧");
        w5.setPronounce("ce");
        w5.setTone("4");
        w5.setProperty("[名][形][动]");
        w5.setExample("他侧身望向了天空");
        w5.save();

        Word w6 = new Word();
        w6.setWord("而");
        w6.setPronounce("er");
        w6.setTone("2");
        w6.setProperty("[副]");
        w6.setExample("他不仅聪明，而且努力学习");
        w6.save();

        Word w7 = new Word();
        w7.setWord("含");
        w7.setPronounce("han");
        w7.setTone("2");
        w7.setProperty("[动]");
        w7.setExample("这本书包含的内容非常广泛");
        w7.save();

        Word w8 = new Word();
        w8.setWord("是");
        w8.setPronounce("shi");
        w8.setTone("4");
        w8.setProperty("[副]");
        w8.setExample("我是一名学生");
        w8.save();

        Word w9 = new Word();
        w9.setWord("紫");
        w9.setPronounce("zi");
        w9.setTone("3");
        w9.setProperty("[名][形]");
        w9.setExample("这本书的封面是紫色的");
        w9.save();

        Vocabulary v0 = new Vocabulary();
        v0.setVocabulary("穷苦");
        v0.setPronounce("qiongku");
        v0.setTone("2,3");
        v0.setProperty("[形]");
        v0.setExample("暴君总是压迫穷苦的百姓");
        v0.save();

        Vocabulary v1 = new Vocabulary();
        v1.setVocabulary("性质");
        v1.setPronounce("xingzhi");
        v1.setTone("4,4");
        v1.setProperty("[名]");
        v1.setExample("他犯了一个性质非常严重的错误");
        v1.save();

        Vocabulary v2 = new Vocabulary();
        v2.setVocabulary("产量");
        v2.setPronounce("chanliang");
        v2.setTone("3,4");
        v2.setProperty("[名]");
        v2.setExample("这个工厂的产量非常惊人");
        v2.save();

        Vocabulary v3 = new Vocabulary();
        v3.setVocabulary("兄弟");
        v3.setPronounce("xiongdi");
        v3.setTone("1,4");
        v3.setProperty("[名]");
        v3.setExample("他有两个兄弟");
        v3.save();

        Vocabulary v4 = new Vocabulary();
        v4.setVocabulary("军队");
        v4.setPronounce("jundui");
        v4.setTone("1,4");
        v4.setProperty("[名]");
        v4.setExample("强大的军队是国家安全的保障");
        v4.save();

        Vocabulary v5 = new Vocabulary();
        v5.setVocabulary("百货");
        v5.setPronounce("baihuo");
        v5.setTone("3,4");
        v5.setProperty("[名]");
        v5.setExample("百货商店里的商品非常得吸引人");
        v5.save();

        Vocabulary v6 = new Vocabulary();
        v6.setVocabulary("摧残");
        v6.setPronounce("cuican");
        v6.setTone("1,2");
        v6.setProperty("[动]");
        v6.setExample("她幼小的心灵收到了摧残");
        v6.save();

        Vocabulary v7 = new Vocabulary();
        v7.setVocabulary("挂号");
        v7.setPronounce("guahao");
        v7.setTone("4,4");
        v7.setProperty("[动]");
        v7.setExample("病人看病之前需要在医院挂号");
        v7.save();

        Vocabulary v8 = new Vocabulary();
        v8.setVocabulary("捐款");
        v8.setPronounce("juankuan");
        v8.setTone("1,3");
        v8.setProperty("[动]");
        v8.setExample("大家纷纷向地震灾区捐款");
        v8.save();

        Vocabulary v9 = new Vocabulary();
        v9.setVocabulary("群众");
        v9.setPronounce("qunzhong");
        v9.setTone("2,4");
        v9.setProperty("[名]");
        v9.setExample("群众的眼睛是雪亮的");
        v9.save();

        Sentence sentence0 = new Sentence();
        sentence0.setSentence("控制紧张情绪的最佳做法是选择你有所了解并感兴趣的话题。");
        sentence0.save();

        Sentence sentence1 = new Sentence();
        sentence1.setSentence("留出足够时间做充分准备还包括要有足够的时间进行练习。");
        sentence1.save();

        Sentence sentence2 = new Sentence();
        sentence2.setSentence("如果体育运动能对我们有所教益的话, 那么这教益便是: 精心的准备能够使运动员获得成功。");
        sentence2.save();

        Sentence sentence3 = new Sentence();
        sentence3.setSentence("研究表明, 在你即将走上台开始讲话的那段时间里, 在你第一次与听众接触的那一刻, 你的恐惧感最为强烈。");
        sentence3.save();

        Sentence sentence4 = new Sentence();
        sentence4.setSentence("夕阳落山不久，西方的天空，还燃烧着一片橘红色的晚霞。大海，也被这霞光染成了红色，而且比天空的景色更要壮观。");
        sentence4.save();

        Sentence sentence5 = new Sentence();
        sentence5.setSentence("最妙的是下点小雪呀。看吧，山上的矮松越发的青黑，树尖儿上顶 // 着一髻儿白花，好像日本看护妇。山尖儿全白了，给蓝天镶上一道银边。");
        sentence5.save();

        Sentence sentence6 = new Sentence();
        sentence6.setSentence("最早出现的启明星，在这蓝色的天幕上闪烁起来了。它是那么大，那么亮，整个广漠的天幕上只有它在那里放射着令人注目的光辉，活像一盏悬挂在高空的明灯。");
        sentence6.save();

        Sentence sentence7 = new Sentence();
        sentence7.setSentence("夜色加浓，苍空中的“明灯”越来越多了。而城市各处的真的灯火也次第亮了起来，尤其是围绕在海港周围山坡上的那一片灯光，从半空倒映在乌蓝的海面上，随着波浪，晃动着，闪烁着，像一串流动着的珍珠，和那一片片密布在苍穹里的星斗互相辉映，煞是好看。");
        sentence7.save();

        Sentence sentence8 = new Sentence();
        sentence8.setSentence("在达瑞八岁的时候，有一天他想去看电影。因为没有钱，他想是向爸妈要钱，还是自己挣钱。最后他选择了后者。他自己调制了一种汽水，向过路的行人出售。可那时正是寒冷的冬天，没有人买，只有两个人例外——他的爸爸和妈妈。");
        sentence8.save();

        Sentence sentence9 = new Sentence();
        sentence9.setSentence("对于一个在北平住惯的人，像我，冬天要是不刮风，便觉得是奇迹;济南的冬天是没有风声的。对于一个刚由伦敦回来的人，像我，冬天要能看得见日光，便觉得是怪事;济南的冬天是响晴的。");
        sentence9.save();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
        LearnedWord lw0 = w0.toLearnedWord();
        lw0.setLastAccess(calendar.getTime());
        lw0.setScore(3.250);
        lw0.save();
        LearnedVocabulary lv0 = v0.toLearnedVocabulary();
        lv0.setLastAccess(calendar.getTime());
        lv0.setScore(3.250);
        lv0.save();
        LearnedSentence ls0 = sentence0.toLearnedSentence();
        ls0.setLastAccess(calendar.getTime());
        ls0.setScore(3.250);
        ls0.save();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        LearnedWord lw1 = w1.toLearnedWord();
        lw1.setLastAccess(calendar.getTime());
        lw1.setScore(2.000);
        lw1.save();
        LearnedVocabulary lv1 = v1.toLearnedVocabulary();
        lv1.setLastAccess(calendar.getTime());
        lv1.setScore(2.000);
        lv1.save();
        LearnedSentence ls1 = sentence1.toLearnedSentence();
        ls1.setLastAccess(calendar.getTime());
        ls1.setScore(2.000);
        ls1.save();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        LearnedWord lw2 = w2.toLearnedWord();
        lw2.setLastAccess(calendar.getTime());
        lw2.setScore(2.750);
        lw2.save();
        LearnedVocabulary lv2 = v2.toLearnedVocabulary();
        lv2.setLastAccess(calendar.getTime());
        lv2.setScore(2.750);
        lv2.save();
        LearnedSentence ls2 = sentence2.toLearnedSentence();
        ls2.setLastAccess(calendar.getTime());
        ls2.setScore(2.750);
        ls2.save();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        LearnedWord lw3 = w3.toLearnedWord();
        lw3.setLastAccess(calendar.getTime());
        lw3.setScore(5.000);
        lw3.save();
        LearnedVocabulary lv3 = v3.toLearnedVocabulary();
        lv3.setLastAccess(calendar.getTime());
        lv3.setScore(5.000);
        lv3.save();
        LearnedSentence ls3 = sentence3.toLearnedSentence();
        ls3.setLastAccess(calendar.getTime());
        ls3.setScore(5.000);
        ls3.save();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        LearnedWord lw4 = w4.toLearnedWord();
        lw4.setLastAccess(calendar.getTime());
        lw4.setScore(4.500);
        lw4.save();
        LearnedVocabulary lv4 = v4.toLearnedVocabulary();
        lv4.setLastAccess(calendar.getTime());
        lv4.setScore(4.500);
        lv4.save();
        LearnedSentence ls4 = sentence4.toLearnedSentence();
        ls4.setLastAccess(calendar.getTime());
        lv4.setScore(4.500);
        ls4.save();
    }

    public void updateData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (pref.getBoolean("isFirstTime", true)) {
            editor.putBoolean("isFirstTime", false);
            String firstTime = sdf.format(new Date());
            editor.putString("firstTime", firstTime);
            editor.apply();
        } else {
            try {
                Date firstTime = sdf.parse(pref.getString("firstTime", sdf.format(new Date())));
//                long l = (new Date()).getTime() - firstTime.getTime();
//                long day = l / (24 * 60 * 60 * 1000);
                List<LearnedWord> lws = DataSupport.findAll(LearnedWord.class);
                List<LearnedVocabulary> lvs = DataSupport.findAll(LearnedVocabulary.class);
                List<LearnedSentence> lss = DataSupport.findAll(LearnedSentence.class);
                days.setText(String.valueOf(alreadyInsist(lws, lvs, lss, firstTime.getTime())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        wordNum = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedWord.class).size();
        vocabularyNum = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedVocabulary.class).size();
        sentenceNum = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedSentence.class).size();

        todayCount.setText(String.valueOf(wordNum + vocabularyNum + sentenceNum));
        int total = DataSupport.findAll(Word.class).size() + DataSupport.findAll(Vocabulary.class).size() + DataSupport.findAll(Sentence.class).size();
        int learned = DataSupport.findAll(LearnedWord.class).size() + DataSupport.findAll(LearnedVocabulary.class).size() + DataSupport.findAll(LearnedSentence.class).size();
        scale.setText(String.valueOf(learned) + "/" + String.valueOf(total));
        progressBar.setMax(total);
        progressBar.setProgress(learned);
    }

    public void showNotification(int i, String title, String content) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_check)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.dark_logo))
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        manager.notify(i, notification);
    }

    public Long alreadyInsist(List<LearnedWord> learnedWords, List<LearnedVocabulary> learnedVocabularies, List<LearnedSentence> learnedSentences, long firstDay) {
        Set<Long> set = new HashSet<>();
        for (LearnedWord learnedWord:learnedWords) {
            set.add((learnedWord.getLastAccess().getTime()-firstDay)/ (24 * 60 * 60 * 1000));
        }
        for (LearnedVocabulary learnedVocabulary:learnedVocabularies) {
            set.add((learnedVocabulary.getLastAccess().getTime()-firstDay)/(24 * 60 * 60 * 1000));
        }
        for (LearnedSentence learnedSentence:learnedSentences) {
            set.add((learnedSentence.getLastAccess().getTime()-firstDay)/(24 * 60 * 60 * 1000));
        }Log.d("insist", String.valueOf(set.size()));
        return (long) set.size();
    }
}
