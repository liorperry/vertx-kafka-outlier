package io.vertx.example.util.parser;

import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.regex.Pattern;

import static io.vertx.example.util.parser.UrlParseResult.*;

/**
 * Taken from </br>
 * <link>http://code.tutsplus.com/tutorials/8-regular-expressions-you-should-know--net-6149</link>
 * <link>http://regexr.com/36upm</link>
 *
 * Description:
 * This regex is almost like taking the ending part of the above regex, slapping it between "http://" and some file structure at the end.
 * It sounds a lot simpler than it really is.
 *
 * To start off, we search for the beginning of the line with the caret.
 * The first capturing group is all option. It allows the URL to begin with "http://", "https://", or neither of them.
 * I have a question mark after the s to allow URL's that have http or https.
 *
 * In order to make this entire group optional, I just added a question mark to the end of it.
 *
 * Next is the domain name: one or more numbers, letters, dots, or hypens followed by another dot then two to six letters or dots.
 *
 * The following section is the optional files and directories. Inside the group, we want to match any number of:
 *          forward slashes, letters, numbers, underscores, spaces, dots, or hyphens.
 * Then we say that this group can be matched as many times as we want.
 * Pretty much this allows multiple directories to be matched along with a file at the end.
 *
 * I have used the star instead of the question mark because the star says zero or more, not zero or one.
 * If a question mark was to be used there, only one file/directory would be able to be matched.
 *
 * Then a trailing slash is matched, but it can be optional. Finally we end with the end of the line.
 *
 */
public class RegExpUrlParser implements UrlParser {

    @Override
    public UrlParseResult parse(String url) {
        String[] split = Patterns.WEB_URL.split(url);
        Arrays.asList(split).forEach(System.out::println);
        return constractResult(split);
    }

    private UrlParseResult constractResult(String[] split) {
        if(split.length != 5) {
            StringBuilder message = new StringBuilder("split size != 5 size("+split.length+")");
            Arrays.asList(split).forEach(s -> {message.append("\n ").append(s);});
            throw new IllegalArgumentException( message.toString() );
        }

        Builder builder = Builder.builder();
        builder.schema(split[0]);
        builder.domain(split[1]);
        builder.port(split[2] != "" ? Optional.of(Integer.valueOf(split[2])) : Optional.<Integer>empty());
        builder.path(split[3]);
        builder.query(split[4] != "" ? Optional.of(split[4]) : Optional.<String>empty());
        return builder.build();
    }


}
