package org.runnect.server.config.featureflag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 미완성/위험도가 있는 기능을 main에 먼저 merge하고 배포 이후에 켤 수 있게 하는
 * 트렁크 기반 개발용 최소 기능 플래그. 값은 environment별 application.yml의
 * feature-flags.flags.{key}로 관리한다 (예: feature-flags.flags.new-run-summary: true).
 */
@Component
@RequiredArgsConstructor
public class FeatureFlags {

    private final FeatureFlagProperties properties;

    public boolean isEnabled(String key) {
        return properties.getFlags().getOrDefault(key, false);
    }
}
