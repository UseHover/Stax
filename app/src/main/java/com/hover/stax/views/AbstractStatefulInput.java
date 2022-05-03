package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import timber.log.Timber;

public abstract class AbstractStatefulInput extends FrameLayout {

    public final static int NONE = 0, INFO = 1, WARN = 2, ERROR = 3, SUCCESS = 4;

    private TextInputLayout inputLayout;
    protected String helperText;

    public AbstractStatefulInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initView() {
        inputLayout = findViewById(R.id.inputLayout);
    }

    protected void setHelperText(String message) {
        helperText = message;
        inputLayout.setHelperText(message);
    }

    public void setError(@Nullable CharSequence errorText) {
        setState(errorText == null ? null : errorText.toString(), errorText == null ? NONE : ERROR);
    }

    public void setState(String message, int state) {
        setHelperText(message);
        switch (state) {
            case INFO:
                setColorAndIcon(R.color.blue_state_color, R.drawable.ic_info);
                break;
            case WARN:
                setColorAndIcon(R.color.yellow_state_color, R.drawable.ic_warning);
                break;
            case SUCCESS:
                setColorAndIcon(R.color.green_state_color, R.drawable.ic_success);
                break;
            case ERROR:
                setColorAndIcon(R.color.red_state_color, R.drawable.ic_error);
                break;
            case NONE:
            default:
                setColorAndIcon(R.color.offwhite_state_color, 0);
                break;
        }
    }

    private void setColorAndIcon(int color, int drawable) {
        if (inputLayout != null) {
            inputLayout.setEndIconDrawable(drawable);
            setColor(color);
        }
    }

    private void setColor(int color) {
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(), getResources().getXml(color));
            inputLayout.setHelperTextColor(csl);
            inputLayout.setEndIconTintList(csl);
            inputLayout.setHintTextColor(csl);
            inputLayout.setBoxStrokeColorStateList(csl);

            inputLayout.setTypeface(ResourcesCompat.getFont(getContext(), R.font.brutalista_regular));
        } catch (IOException | XmlPullParserException | NullPointerException e) {
            Timber.e(e, "Failed to load color state list");
        }
    }

//  The below is from http://web.archive.org/web/20180625034135/http://trickyandroid.com/saving-android-view-state-correctly/
//  it prevents views with non-unique ids from overwriting each other
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
            = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
