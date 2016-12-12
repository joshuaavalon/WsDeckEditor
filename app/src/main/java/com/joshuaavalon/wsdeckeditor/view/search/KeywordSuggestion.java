package com.joshuaavalon.wsdeckeditor.view.search;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.common.collect.Sets;
import com.joshuaavalon.wsdeckeditor.sdk.card.Filter;

public class KeywordSuggestion extends AbstractSuggestion {
    public static final Creator<KeywordSuggestion> CREATOR = new Creator<KeywordSuggestion>() {
        @Override
        public KeywordSuggestion createFromParcel(Parcel source) {
            return new KeywordSuggestion(source);
        }

        @Override
        public KeywordSuggestion[] newArray(int size) {
            return new KeywordSuggestion[size];
        }
    };
    @NonNull
    private final String keywords;

    public KeywordSuggestion(@NonNull final String keywords) {
        this.keywords = keywords;
    }

    protected KeywordSuggestion(Parcel in) {
        this.keywords = in.readString();
    }

    @Override
    public String getBody() {
        return keywords;
    }

    @Override
    public Filter toFilter(final boolean isNormalOnly) {
        final Filter filter = new Filter();
        filter.setKeyword(Sets.newHashSet(keywords.split("\\s+")));
        filter.setHasChara(true);
        filter.setHasSerial(true);
        filter.setHasName(true);
        filter.setNormalOnly(isNormalOnly);
        return filter;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeywordSuggestion)) return false;
        KeywordSuggestion that = (KeywordSuggestion) o;
        return keywords.equals(that.keywords);
    }
}
