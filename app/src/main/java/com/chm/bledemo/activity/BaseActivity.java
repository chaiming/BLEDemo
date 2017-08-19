package com.chm.bledemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.chm.bledemo.R;
import com.gyf.barlibrary.ImmersionBar;

/**
 * 类名: BaseActivity
 * 作者: 陈海明
 * 时间: 2017-08-10 11:58
 * 描述: NULL
 */
public class BaseActivity extends AppCompatActivity {
    public ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImmersionBar = ImmersionBar.with(this);
        statusBarColor();
    }
    /**
     * [页面跳转]
     *
     * @param mainActivity
     * @param clz
     */
    public void startActivity(MainActivity mainActivity, Class<?> clz) {
        startActivity(new Intent(BaseActivity.this,clz));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImmersionBar.destroy();  //不调用该方法，如果界面状态栏发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
    }
  public void statusBarColor(){
      mImmersionBar.statusBarColor(R.color.mediumaquamarine)
              .init();
  }
}
