package org.runnect.server.config.featureflag;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yml의 feature-flags.flags.{key}: true/false 값을 그대로 바인딩한다.
 * yml에 없는 키는 FeatureFlags에서 false(비활성)로 취급한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlagProperties {

    private Map<String, Boolean> flags = new HashMap<>();
}
