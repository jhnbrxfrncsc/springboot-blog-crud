package dev.mini.project.blog.common;

import java.util.function.Consumer;

public class UpdateHelper {

    private UpdateHelper() {}

    public static boolean updateIfChanged(
            String newValue,
            String currentValue,
            Consumer<String> setter
    ) {
        if (newValue != null && !newValue.isBlank() && !newValue.equals(currentValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }
}
