package skierMS;

import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class JedisPoolConfiguration {
    private JedisPoolConfig poolConfig;
    public JedisPoolConfiguration() {
        this.poolConfig = buildPoolConfig();
    }

    public static JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(64);
        poolConfig.setMaxIdle(64);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public JedisPoolConfig getPoolConfig(){
        return this.poolConfig;
    }
}
