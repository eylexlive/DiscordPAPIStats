package io.github.eylexlive.discordpapistats.util;

import java.util.List;
import java.util.regex.Matcher;

public final class ReplaceUtil {

    public static void replacePlaceholders(List<String> list, String... placeholders) {
        for (String str : placeholders) {
            final String placeholder = str.split(":")[0];
            final String value = str.replaceFirst(Matcher.quoteReplacement(placeholder + ":"), "");
            list.replaceAll(s -> s.replace("{" + Matcher.quoteReplacement(placeholder) + "}", value));
        }
    }
}
