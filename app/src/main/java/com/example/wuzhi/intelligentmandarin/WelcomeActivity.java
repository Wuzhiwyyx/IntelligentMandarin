package com.example.wuzhi.intelligentmandarin;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager vp_guide;
    private List<ImageView> mImgList;//导航图集合
    private LinearLayout ll_container;//小圆点容器
    private int mCurrentIndex = 0;//当前小圆点的位置
    private int[] imgArray = {R.drawable.exclusive_bg, R.drawable.head_sculpture, R.drawable.nav_header_bg, R
            .drawable.dark_logo};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        vp_guide = (ViewPager) findViewById(R.id.vp_guide);
        ll_container = (LinearLayout) findViewById(R.id.ll_container);
        mImgList = new ArrayList<>();
        for (int i = 0; i < imgArray.length; i++) {
            ImageView imageView = new ImageView(this);//获取4个圆点
            imageView.setImageResource(imgArray[i]);
            mImgList.add(imageView);
            ImageView dot = new ImageView(this);
            if (i == mCurrentIndex) {
                dot.setImageResource(R.drawable.ic_check_point);//设置当前页的圆点
            } else {
                dot.setImageResource(R.drawable.ic_uncheck_point);//其余页的圆点
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                    .LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.leftMargin = 10;//设置圆点边距
            }
            dot.setLayoutParams(params);
            ll_container.addView(dot);//将圆点添加到容器中
        }
        vp_guide.setAdapter(new MyAdapter(mImgList));
        //添加监听
        vp_guide.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int
                    positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //根据监听的页面改变当前页对应的小圆点
                mCurrentIndex = position;
                for (int i = 0; i < ll_container.getChildCount(); i++) {
                    ImageView imageView = (ImageView) ll_container.getChildAt(i);
                    if (i == position) {
                        imageView.setImageResource(R.drawable.ic_check_point);
                    } else {
                        imageView.setImageResource(R.drawable.ic_uncheck_point);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class MyAdapter extends PagerAdapter {
        private List<ImageView> mViewList;

        public MyAdapter(List<ImageView> viewList) {
            mViewList = viewList;
        }

        @Override
        public ImageView instantiateItem(ViewGroup container, int position) {
            ImageView view = mViewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }
    }
}