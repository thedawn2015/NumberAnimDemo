package com.app.simon.numberlib;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * desc:
 * date: 2017/8/7
 *
 * @author xw
 */
public class NumberAnimTextView extends AppCompatTextView {

    /** 起始数字 */
    private String numStart;
    /** 结束数字 */
    private String numEnd;
    /** 是否是整数 */
    private boolean isInt;

    public NumberAnimTextView(Context context) {
        this(context, null, 0);
    }

    public NumberAnimTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberAnimTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberAnimTextView, defStyleAttr, 0);
        if (typedArray != null) {
            numStart = typedArray.getString(R.styleable.NumberAnimTextView_numStart);
            numEnd = typedArray.getString(R.styleable.NumberAnimTextView_numEnd);
        } else {
            numStart = "";
            numEnd = "";
        }
        // FIXME: 2017/8/7 by xw TODO: 再补充其它的参数

        setNumString(numStart, numEnd);
    }

    /**
     * 设置数字范围
     *
     * @param numStart
     * @param numEnd
     */
    public void setNumString(String numStart, String numEnd) {
        if (isNumStringValid(numStart, numEnd)) {
            start();
        } else {
            setText(numEnd);
        }
    }

    /**
     * 开始
     */
    private void start() {
        ValueAnimator animator = ValueAnimator.ofObject(new BigDecimalEvaluator(), new BigDecimal(numStart), new BigDecimal(numEnd));
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                BigDecimal value = (BigDecimal) animation.getAnimatedValue();
                setText(format(value));
//                setText(mPrefixString + format(value) + mPostfixString);
            }
        });
        animator.start();
    }

    /**
     * 格式化 BigDecimal ,小数部分时保留两位小数并四舍五入
     *
     * @param decimal
     *         　BigDecimal
     * @return 格式化后的 String
     */
    private String format(BigDecimal decimal) {
        StringBuilder pattern = new StringBuilder();
        if (isInt) {
            pattern.append("#,###");
        } else {
            int length = 0;
            String decimals = numEnd.split("\\.")[1];
            if (decimals != null) {
                length = decimals.length();
            }
            pattern.append("#,##0");
            if (length > 0) {
                pattern.append(".");
                for (int i = 0; i < length; i++) {
                    pattern.append("0");
                }
            }
        }
        DecimalFormat df = new DecimalFormat(pattern.toString());
        return df.format(decimal);
    }

    /**
     * 数字是否有效
     *
     * @param numStart
     * @param numEnd
     * @return
     */
    private boolean isNumStringValid(String numStart, String numEnd) {
        String regexInteger = "-?\\d*";
        //是否是整数
        isInt = numStart.matches(regexInteger) && numStart.matches(regexInteger);
        if (isInt) {
            BigInteger start = new BigInteger(numStart);
            BigInteger end = new BigInteger(numEnd);
            return end.compareTo(start) >= 0;
        }
        //不是整数
        String regexDecimal = "-?[1-9]\\d*.\\d*|-?0.\\d*[1-9]\\d*";
        if ("0".equals(numStart)) {
            if (numEnd.matches(regexDecimal)) {
                BigDecimal start = new BigDecimal(numStart);
                BigDecimal end = new BigDecimal(numEnd);
                return end.compareTo(start) > 0;
            }
        }
        if (numEnd.matches(regexDecimal) && numStart.matches(regexDecimal)) {
            BigDecimal start = new BigDecimal(numStart);
            BigDecimal end = new BigDecimal(numEnd);
            return end.compareTo(start) > 0;
        }
        return false;
    }

    // 不加 static 关键字，也不会引起内存泄露，因为这里也没有开启线程
    // 加上 static 关键字，是因为该内部类不需要持有外部类的引用，习惯加上
    private class BigDecimalEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            BigDecimal start = (BigDecimal) startValue;
            BigDecimal end = (BigDecimal) endValue;
            BigDecimal result = end.subtract(start);
            return result.multiply(new BigDecimal("" + fraction)).add(start);
        }
    }
}
