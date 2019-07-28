package com.shiro.demo.shiro;

import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * RedisSessionDao
 *
 * @author zhangkuan
 * @Date 2019/7/27
 */
@Component
public class RedisSessionDao extends AbstractSessionDAO {

    private Logger log = Logger.getLogger(this.getClass());

    @Value("${session.redis.expireTime}")
    private long expireTime;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected Serializable doCreate(Session session) {
        log.debug("====doCreate:\t\t" + session.getId() + "=======");
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        redisTemplate.opsForValue().set(session.getId(), session, expireTime, TimeUnit.SECONDS);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        log.debug("====doReadSession:\t" + sessionId + "========");
        return sessionId == null ? null : (Session) redisTemplate.opsForValue().get(sessionId);
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        log.debug("====update:\t\t\t" + session.getId() + "=======");
        if (session != null && session.getId() != null) {
            session.setTimeout(expireTime * 1000);
            redisTemplate.opsForValue().set(session.getId(), session, expireTime, TimeUnit.SECONDS);
        }
    }

    @Override
    public void delete(Session session) {
        log.debug("====delete:\t\t\t" + session.getId() + "=======");
        if (session != null && session.getId() != null) {
            redisTemplate.opsForValue().getOperations().delete(session.getId());
        }
    }

    @Override
    public Collection<Session> getActiveSessions() {
        log.debug("====getActiveSessions==========");
        return redisTemplate.keys("*");
    }

}
