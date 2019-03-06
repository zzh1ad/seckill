package org.seckill.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SeckillDao seckillDao;

	@Autowired
	private RedisDao redisDao;

	@Autowired
	private SuccessKilledDao successKilledDao;

	private final String slat = "wjrn298Y7HDIWH2jjnlS,.<mu*yr$#edfgHHAK";

	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	public Exposer exportSeckillUrl(long seckillId) {
		//缓存优化
		Seckill seckill = redisDao.getSeckill(seckillId);
		if(seckill == null){
			seckill = seckillDao.queryById(seckillId);
			if (seckill == null){
				return new Exposer(false, seckillId);

			} else {
				redisDao.putSeckill(seckill);
			}
		}
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		Date nowTime = new Date();
		if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}

		String md5 = getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}

	private String getMD5(long seckillId) {
		String base = seckillId + "/" + slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	@Transactional
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		if (md5 == null || !md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill data rewrite");
		}
		// 执行秒杀逻辑:减库存 记录购买行为
		Date nowTime = new Date();
		try {
			int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
			if (insertCount <= 0) {
				// 重复秒杀
				throw new RepeatKillException("seckill repeated");
			} else {
				int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
				if (updateCount <= 0) {
					// 没有更新记录
					throw new SeckillCloseException("seckill is closed");
				} else {
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
				}

			}

		} catch (SeckillCloseException e1) {
			throw e1;
		} catch (RepeatKillException e2) {
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new SeckillException("seckill inner error:"+e.getMessage());
		}
	}

	public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
		if(md5 == null || !md5.equals(getMD5(seckillId))){
			return  new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);

		}
		Date killTime = new Date();
		logger.info("killTime:"+killTime);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("seckillId",seckillId);
		map.put("phone",userPhone);
		map.put("killTime",killTime);
		map.put("result",-3);
		//执行存储过程
		try {
			seckillDao.killByProcedure(map);
			int result = MapUtils.getInteger(map,"result",-2);
			if (result ==1) {
				SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
				return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);
			} else{
				return new SeckillExecution(seckillId,SeckillStatEnum.stateof(result));
			}
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
		}
	}


}
