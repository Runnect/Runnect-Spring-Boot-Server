package org.runnect.server.config.featureflag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FeatureFlagsTest {

    @Test
    void yml에_true로_설정된_플래그는_활성이다() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.setFlags(Map.of("new-run-summary", true));
        FeatureFlags featureFlags = new FeatureFlags(properties);

        assertThat(featureFlags.isEnabled("new-run-summary")).isTrue();
    }

    @Test
    void yml에_false로_설정된_플래그는_비활성이다() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.setFlags(Map.of("new-run-summary", false));
        FeatureFlags featureFlags = new FeatureFlags(properties);

        assertThat(featureFlags.isEnabled("new-run-summary")).isFalse();
    }

    @Test
    void yml에_정의되지_않은_플래그는_기본값으로_비활성이다() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        FeatureFlags featureFlags = new FeatureFlags(properties);

        assertThat(featureFlags.isEnabled("존재하지-않는-플래그")).isFalse();
    }
}
