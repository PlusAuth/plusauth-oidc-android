package com.plusauth.android.customtabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.content.ContextCompat;

/**
 * This class holds customization options for controlling appearance of CustomTabs.
 */
public class CustomTabsOptions implements Parcelable {

    private final boolean showTitle;
    @ColorRes
    private final int toolbarColor;

    private CustomTabsOptions(boolean showTitle, @ColorRes int toolbarColor) {
        this.showTitle = showTitle;
        this.toolbarColor = toolbarColor;
    }

    public static final Creator<CustomTabsOptions> CREATOR = new Creator<CustomTabsOptions>() {
        @Override
        public CustomTabsOptions createFromParcel(Parcel in) {
            return new CustomTabsOptions(in);
        }

        @Override
        public CustomTabsOptions[] newArray(int size) {
            return new CustomTabsOptions[size];
        }
    };

    public static Builder newBuilder() {
        return new Builder();
    }

    @SuppressLint("ResourceType")
    Intent toIntent(Context context, CustomTabsSession session) {
        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session)
                .setShowTitle(showTitle);
        if (toolbarColor > 0) {
            builder.setToolbarColor(ContextCompat.getColor(context, toolbarColor));
        }
        return builder.build().intent;
    }

    public static class Builder {
        @ColorRes
        private int toolbarColor;
        private boolean showTitle;

        private Builder() {
            this.showTitle = false;
            this.toolbarColor = 0;
        }

        /**
         * Customizes CustomTab toolbar color
         *
         * @param toolbarColor new color
         * @return this
         */
        public Builder withToolbarColor(@ColorRes int toolbarColor) {
            this.toolbarColor = toolbarColor;
            return this;
        }

        /**
         * Whether to make the Custom Tab show the Page Title in the toolbar or not.
         * By default, the Page Title will be hidden.
         *
         * @param showTitle whether to show the Page Title in the toolbar or not.
         * @return this
         */
        public Builder showTitle(boolean showTitle) {
            this.showTitle = showTitle;
            return this;
        }

        public CustomTabsOptions build() {
            return new CustomTabsOptions(showTitle, toolbarColor);
        }
    }

    protected CustomTabsOptions(Parcel in) {
        showTitle = in.readByte() != 0;
        toolbarColor = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (showTitle ? 1 : 0));
        dest.writeInt(toolbarColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
