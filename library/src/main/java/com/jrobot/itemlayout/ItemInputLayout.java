package com.jrobot.itemlayout;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;

@InverseBindingMethods(
        {
                @InverseBindingMethod(type = ItemInputLayout.class,
                        attribute = "contentText",
                        method = "getContentText",
                        event = "contentTextAttrChanged"
                )
        }
)
public class ItemInputLayout extends LinearLayout implements TextWatcher {
    private CharSequence labelText;
    private CharSequence contentText;
    private TextView mLabelView;
    private EditText mEditText;
    private boolean canInput = false;
    private OnClickListener mOnClickListener;
    private boolean contentClickable = false;
    private CharSequence labelSuffix;
    private boolean required;
    private int contentMaxLines;

    public ItemInputLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public ItemInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ItemInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(getContext()).inflate(R.layout._item_input_layout_, this);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ItemInputLayout, defStyleAttr, R.style.Widget_AppTheme_ItemInputView);
        int padding = (int) a.getDimension(R.styleable.ItemInputLayout_padding, 15);
        int textSize = a.getDimensionPixelSize(R.styleable.ItemInputLayout_strSize, 15);
        int contentMarginStart = (int) a.getDimension(R.styleable.ItemInputLayout_contentMarginStart, 0);
        int labelWidth = (int) a.getDimension(R.styleable.ItemInputLayout_labelWidth, 0);
        int contentColor = a.getColor(R.styleable.ItemInputLayout_contentColor, getResources().getColor(R.color.gary66));
        int labelColor = a.getColor(R.styleable.ItemInputLayout_labelColor, getResources().getColor(R.color.gary66));
        contentClickable = a.getBoolean(R.styleable.ItemInputLayout_contentClickable, contentClickable);
        contentMaxLines = a.getInteger(R.styleable.ItemInputLayout_contentMaxLines, -1);

        CharSequence hint = a.getString(R.styleable.ItemInputLayout_hint);

        if (hint == null) {
            hint = labelText;
        }
        String contentGravity = a.getString(R.styleable.ItemInputLayout_content_gravity);


        canInput = a.getBoolean(R.styleable.ItemInputLayout_inputable, canInput);

        int labelRes = a.getResourceId(R.styleable.ItemInputLayout_label, Resources.ID_NULL);

        if (labelRes != Resources.ID_NULL) {
            labelText = Html.fromHtml(getContext().getString(labelRes));
        } else {
            labelText = a.getString(R.styleable.ItemInputLayout_label);
        }

        int suffixRes = a.getResourceId(R.styleable.ItemInputLayout_labelSuffix, Resources.ID_NULL);

        if (suffixRes != Resources.ID_NULL) {
            labelSuffix = getContext().getString(suffixRes);
        } else {
            labelSuffix = a.getString(R.styleable.ItemInputLayout_labelSuffix);
        }

        required = a.getBoolean(R.styleable.ItemInputLayout_required, false);

        contentText = a.getString(R.styleable.ItemInputLayout_contentText);
        mLabelView = findViewById(R.id.tv_title);
        mLabelView.setGravity(Gravity.CENTER_VERTICAL);
        mLabelView.setPadding(0, padding, 0, padding);
        mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mLabelView.setTextColor(labelColor);
        updateLabelText();
        ViewGroup.LayoutParams params = mLabelView.getLayoutParams();
        if (labelWidth != 0) {
            params.width = labelWidth;
        }
        mLabelView.setLayoutParams(params);

        mEditText = findViewById(R.id.et_content);
        mEditText.setBackground(null);
        mEditText.setGravity(Gravity.CENTER_VERTICAL);
        mEditText.setPadding(0, padding, padding, padding);
        mEditText.setText(contentText);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mEditText.setTextColor(contentColor);
        mEditText.setFocusable(canInput);
        mEditText.setHint(hint);
        mEditText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!canInput) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        post(onClickRunnable);
                    }
                    return true;
                }
                return v.onTouchEvent(event);
            }
        });

        updateMaxLines();

        mEditText.addTextChangedListener(this);
        ConstraintLayout.LayoutParams mEditTextLayoutParams = (ConstraintLayout.LayoutParams) mEditText.getLayoutParams();
        int gravity = -1;
        switch (contentGravity) {
            case "0":
                gravity = Gravity.START;
                break;
            case "1":
                gravity = Gravity.CENTER;
                break;
            case "2":
                gravity = Gravity.END;
                break;
        }
        if (gravity != -1) {
            mEditText.setGravity(gravity);
        }
        mEditTextLayoutParams.leftMargin = contentMarginStart;
        mEditText.setLayoutParams(mEditTextLayoutParams);
        a.recycle();
        if (canInput) {
            mEditText.setMaxLines(1);
        }
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public void setContentMaxLines(int contentMaxLines) {
        this.contentMaxLines = contentMaxLines;
        updateMaxLines();
    }

    public int getContentMaxLines() {
        contentMaxLines = mEditText.getMaxLines();
        return contentMaxLines;
    }

    private void updateMaxLines() {
        if (contentMaxLines != -1) {
            mEditText.setMaxLines(contentMaxLines);
            int inputType = mEditText.getInputType();
            if (contentMaxLines == 1) {
                inputType ^= EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
            } else if (contentMaxLines > 1) {
                inputType |= EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
            }
            mEditText.setInputType(inputType);
        }
    }

    private void updateLabelText() {
        String str = String.valueOf(labelText);
        if (!TextUtils.isEmpty(labelText) && !TextUtils.isEmpty(labelSuffix)) {
            str = str.concat(labelSuffix.toString());
        }
        if (required) {
            mLabelView.setText(Html.fromHtml(getContext().getString(R.string.required, str)));
        } else {
            mLabelView.setText(str);
        }
    }

    public void setLabelText(CharSequence label) {
        this.labelText = label;
        updateLabelText();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mEditText.isFocused() && !TextUtils.isEmpty(mEditText.getText())) {
            mEditText.setSelection(mEditText.getText().length());
        }
    }

    public TextView getLabelView() {
        return mLabelView;
    }

    public TextView getContentView() {
        return mEditText;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (!contentClickable && !canInput) || super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(onClickRunnable);
    }

    private Runnable onClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mEditText);
            }
        }
    };

    public void setOnContentClickListener(OnClickListener listener) {
        mOnClickListener = listener;
        contentClickable = true;
        canInput = false;
    }

    @BindingAdapter("contentText")
    public static void setContentText(ItemInputLayout view, CharSequence text) {
        view.setContentText(text);
    }

    public String getContentText() {
        return mEditText.getText() == null ? "" : mEditText.getText().toString();
    }

    private InverseBindingListener mListener;

    public void setListener(InverseBindingListener listener) {
        mListener = listener;
    }

    public void setContentText(CharSequence text) {
        contentText = text;
        mEditText.setText(text);
        notifyContextChanged();
    }

    private void notifyContextChanged() {
        if (mListener != null) {
            mListener.onChange();
        }
    }

    @BindingAdapter("contentTextAttrChanged")
    public static void setContentChangedListener(ItemInputLayout view, InverseBindingListener listener) {
        if (listener == null) {
            view.setListener(null);
        } else {
            view.setListener(listener::onChange);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
//        contentText = s;
        notifyContextChanged();
    }
}
