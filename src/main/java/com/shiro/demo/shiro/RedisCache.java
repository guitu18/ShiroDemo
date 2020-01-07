package com.shiro.demo.shiro;

import com.shiro.demo.entity.User;
import org.apache.log4j.Logger;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存支持
 *
 * @author zhangkuan
 * @date 2019/8/8
 */
@Component
public class RedisCache<K, V> implements Cache<K, V> {

    private Logger log = Logger.getLogger(this.getClass());

    @Value("${session.redis.expireTime}")
    private long expireTime;

    private final String SHIRO_CACHE_PREFIX = "SHIRO_CACHE:";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取Shiro中保存的用户ID
     * 该值是在登录认证时我们保存到AuthenticationInfo中的
     * 这里我们保存的是当前登录的User实例
     *
     * @param k
     * @return
     */
    private String getUserId(K k) {
        PrincipalCollection principal = (PrincipalCollection) k;
        return ((User) principal.getPrimaryPrincipal()).getId().toString();
    }

    @Override
    public V get(K k) throws CacheException {
        System.out.println(">>>>>>>>>>> get");
        if (k instanceof PrincipalCollection) {
            return (V) redisTemplate.opsForValue().get(SHIRO_CACHE_PREFIX + getUserId(k));
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        System.out.println(">>>>>>>>>>> put");
        if (k instanceof PrincipalCollection) {
            redisTemplate.opsForValue().set(SHIRO_CACHE_PREFIX + getUserId(k), v);
            redisTemplate.expire(SHIRO_CACHE_PREFIX + getUserId(k), expireTime, TimeUnit.SECONDS);
            return v;
        }
        return null;
    }

    @Override
    public V remove(K k) throws CacheException {
        System.out.println(">>>>>>>>>>> remove");
        if (k instanceof PrincipalCollection) {
            V v = get(k);
            redisTemplate.delete(SHIRO_CACHE_PREFIX + getUserId(k));
            return v;
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {
        System.out.println(">>>>>>>>>>> clear");
    }

    @Override
    public int size() {
        System.out.println(">>>>>>>>>>> size");
        return 0;
    }

    @Override
    public Set<K> keys() {
        System.out.println(">>>>>>>>>>> keys");
        return null;
    }

    @Override
    public Collection<V> values() {
        System.out.println(">>>>>>>>>>> values");
        return null;
    }
}
