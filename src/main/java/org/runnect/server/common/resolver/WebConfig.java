package org.runnect.server.common.resolver;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.resolver.userId.UserIdResolver;
import org.runnect.server.common.resolver.sort.SortStatusIdResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserIdResolver userIdxResolver;
    private final SortStatusIdResolver sortStatusIdResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        resolvers.add(userIdxResolver);
        resolvers.add(sortStatusIdResolver);
    }
}
