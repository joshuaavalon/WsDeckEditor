package com.joshuaavalon.wsdeckeditor;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static <T extends Enum<T> & StringResource> List<String> getStringResourceList(Class<T> enumType) {
        final List<String> strings = new ArrayList<>();
        for (T value : enumType.getEnumConstants())
            strings.add(WsApplication.getContext().getString(value.getResId()));
        return strings;
    }
}
