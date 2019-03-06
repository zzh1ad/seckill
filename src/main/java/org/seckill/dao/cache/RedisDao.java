package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @ClassName RedisDao
 * @Description TODO
 * @Author zhangzhaohui
 * @Date 2019/2/10 15:08
 * @Version 1.0
 **/
public class RedisDao {

    private JedisPool jedisPool;

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
        logger.info("ip="+ip);
        logger.info("port="+port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Seckill getSeckill(long seckillId){
        Jedis jedis = jedisPool.getResource();
        try{
            String key = "seckill:"+seckillId;
            //没有实现内部序列化
            //get->byte[]->反序列化
            //采用自定义序列化
            byte[] bytes = jedis.get(key.getBytes());
            if(bytes != null){
                Seckill seckill = schema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                //seckill被反序列化
                return  seckill;
            }
        } finally {
            jedis.close();

        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        //set Object(seckill)->序列化->byte[]
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout = 60*60;
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return  result;
            } finally {
                jedis.close();

            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);

        }
        return null;
    }
}
