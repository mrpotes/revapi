package org.revapi.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.junit.Test;

import org.revapi.API;
import org.revapi.Archive;
import org.revapi.ChangeSeverity;
import org.revapi.CompatibilityType;
import org.revapi.Configuration;
import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.simple.SimpleElement;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
public class ClassificationTransformTest {

    private static class DummyElement extends SimpleElement {

        private final String name;

        public DummyElement(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        @SuppressWarnings("ConstantConditions")
        public API getApi() {
            return null;
        }

        @Nonnull
        @Override
        public String getFullHumanReadableString() {
            return name;
        }

        @Override
        public int compareTo(@Nonnull Element o) {
            if (!(o instanceof DummyElement)) {
                return -1;
            }

            return name.compareTo(((DummyElement) o).name);
        }
    }

    private static API emptyAPI() {
        return new API(Collections.<Archive>emptyList(), Collections.<Archive>emptyList());
    }

    @Test
    public void test() throws Exception {
        DummyElement oldE = new DummyElement("old");
        DummyElement newE = new DummyElement("new");

        Difference difference = Difference.builder().withCode("code").addClassification(
            CompatibilityType.BINARY, ChangeSeverity.NON_BREAKING).addClassification(CompatibilityType.SOURCE,
            ChangeSeverity.POTENTIALLY_BREAKING).build();

        Configuration config = new Configuration(Locale.getDefault(), new HashMap<String, String>(), emptyAPI(),
            emptyAPI());
        config.getProperties().put("revapi.reclassify.1.code", "code");
        config.getProperties().put("revapi.reclassify.1.BINARY", "BREAKING");

        try (ClassificationTransform t = new ClassificationTransform()) {
            t.initialize(config);
            difference = t.transform(oldE, newE, difference);
            assert difference != null &&
                difference.classification.get(CompatibilityType.BINARY) == ChangeSeverity.BREAKING;
            assert difference != null &&
                difference.classification.get(CompatibilityType.SOURCE) == ChangeSeverity.POTENTIALLY_BREAKING;
        }
    }
}