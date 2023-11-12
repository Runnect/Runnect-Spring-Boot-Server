package org.runnect.server.common.resolver.sort;


import org.runnect.server.common.constant.SortStatus;
import org.runnect.server.common.constant.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SortStatusIdResolver implements HandlerMethodArgumentResolver{
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SortStatusId.class) && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String queryString = request.getQueryString();
        Map<String, List<String>> splitQuery = Util.splitQuery(queryString);


        if(!splitQuery.containsKey("sort") || splitQuery.get("sort").get(0)==null){
            // 1번째 조건 : ~url~/public-course
            // 2번째 조건 : ~url~/public-course?sort=
            //정렬할 기준(미입력시 자동으로 최신순)
            return "date";
        }

        if(!Arrays.stream(SortStatus.values())
                .map(sortStatus -> sortStatus.getVlaue())
                .collect(Collectors.toList())
                .contains(splitQuery.get("sort").get(0))){
            throw new BadRequestException(ErrorStatus.INVALID_SORT_PARAMETER_EXCEPTION,ErrorStatus.INVALID_SORT_PARAMETER_EXCEPTION.getMessage());
        }

        return splitQuery.get("sort").get(0);


    }


    static class Util {
        public static Map<String, List<String>> splitQuery(String query_string) {
            final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
            final String[] pairs = query_string.split("&");
            try {
                for (String pair : pairs) {
                    final int idx = pair.indexOf("=");
                    String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    if (!query_pairs.containsKey(key)) query_pairs.put(key, new LinkedList<String>());
                    final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                    query_pairs.get(key).add(value);
                }
                return query_pairs;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
