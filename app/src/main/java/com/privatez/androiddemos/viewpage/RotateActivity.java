package com.privatez.androiddemos.viewpage;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.privatez.androiddemos.R;
import com.privatez.androiddemos.base.BaseActivity;

/**
 * Created by private on 2017/6/12.
 */

public class RotateActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvPrev;
    private TextView tvNext;
    private RelativeLayout rlContent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);

        tvPrev = (TextView) findViewById(R.id.tv_prev);
        tvNext = (TextView) findViewById(R.id.tv_next);
        rlContent = (RelativeLayout) findViewById(R.id.rl_content);

        tvPrev.setOnClickListener(this);
        tvNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_prev:
                prev();
                break;
            case R.id.tv_next:
                next();
                break;
        }
    }

    private void prev() {
        float centerX = rlContent.getWidth() / 2f;
        float centerY = rlContent.getHeight() / 2f;
        // 构建3D旋转动画对象，旋转角度为0到90度，这使得ListView将会从可见变为不可见
        final Rotate3dAnimation rotation = new Rotate3dAnimation(0, 5, centerX, centerY,
                10f, true);
        // 动画持续时间500毫秒
        rotation.setDuration(500);
        // 动画完成后保持完成的状态
        rotation.setFillAfter(false);
        rotation.setInterpolator(new AccelerateInterpolator());
        // 设置动画的监听器
        //rotation.setAnimationListener(new TurnToImageView());
        rlContent.startAnimation(rotation);
    }

    private void next() {
        float centerX = rlContent.getWidth() / 2f;
        float centerY = rlContent.getHeight() / 2f;
        // 构建3D旋转动画对象，旋转角度为0到90度，这使得ListView将会从可见变为不可见
        final Rotate3dAnimation rotation = new Rotate3dAnimation(0, 90, centerX, centerY,
                310.0f, true);
        // 动画持续时间500毫秒
        rotation.setDuration(500);
        // 动画完成后保持完成的状态
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        // 设置动画的监听器
        rotation.setAnimationListener(new TurnToImageView());
        rlContent.startAnimation(rotation);
    }

    class TurnToImageView implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        /**
         * 当ListView的动画完成后，还需要再启动ImageView的动画，让ImageView从不可见变为可见
         */
        @Override
        public void onAnimationEnd(Animation animation) {
            // 获取布局的中心点位置，作为旋转的中心点
            float centerX = rlContent.getWidth() / 2f;
            float centerY = rlContent.getHeight() / 2f;
            // 构建3D旋转动画对象，旋转角度为270到360度，这使得ImageView将会从不可见变为可见
            final Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY,
                    310.0f, false);
            // 动画持续时间500毫秒
            rotation.setDuration(500);
            // 动画完成后保持完成的状态
            rotation.setFillAfter(true);
            rotation.setInterpolator(new AccelerateInterpolator());
            rlContent.startAnimation(rotation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }
}
