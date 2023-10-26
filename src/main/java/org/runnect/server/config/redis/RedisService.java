package org.runnect.server.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate; //RedisTemplate<K,V>


    //레디스에 키-벨류 설정
    public void setValues(String key, String value, long expiryTime){
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(expiryTime));
    }

    //키값으로 vlaue 가져오기
    public String getValuesByKey(String key){
        return redisTemplate.opsForValue().get(key);
    }

    //키 값으로 vlaue 삭제
    public void deleteValueByKey(String key){
        redisTemplate.delete(key);
    }


}

