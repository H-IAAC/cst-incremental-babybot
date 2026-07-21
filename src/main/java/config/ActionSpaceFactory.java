package config;

import java.util.List;
import java.util.stream.Collectors;
public final class ActionSpaceFactory {

    private ActionSpaceFactory() {
    }

    public static List<String> create(ActionSet actionSet) {

        List<String> basic = List.of(
                "am1",
                "am2",
                "am3",
                "am4",
                "am5",
                "am6",
                "am7",
                "am8",
                "am9",
                "am10"
        );

        if (actionSet == ActionSet.BASIC) {
            return basic;
        }

        List<String> orientation = List.of(
                "am11",
                "am12",
                "am13",
                "am14"
        );

        if (actionSet == ActionSet.BASIC_WITH_ORIENTATION) {
            return java.util.stream.Stream
                    .concat(basic.stream(), orientation.stream())
                    .collect(Collectors.toList());
        }

        List<String> topDown = List.of(
                "am15",
                "am16",
                "am17"
        );

        return java.util.stream.Stream.of(
                        basic,
                        orientation,
                        topDown
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}