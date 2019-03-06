package org.seckill.service;

import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

/**
 * 使用者角度设计接口
 * 1.方法定义粒度
 * 2.参数
 * 3.返回类型   return 类型/异常
 * @author zzh
 *
 */

public interface SeckillService {

	/**
	 * 查询所有秒杀记录
	 * @return
	 */
	List<Seckill> getSeckillList();
	
	/**
	     * 查询单个秒杀记录
	 * @param seckillId
	 * @return
	 */
	Seckill getById(long seckillId);
	
	/**
	 * 秒杀开启时输出秒杀接口地址
	 * 否则输出系统时间和秒杀时间
	 * @param seckillId
	 * @return
	 */
	Exposer exportSeckillUrl(long seckillId);
	
	/**
	 * 执行秒杀
	 * @param seckillId
	 * @param md5
	 */
	SeckillExecution executeSeckill(long seckillId,long userPhone,String md5)
	throws SeckillException,RepeatKillException,SeckillCloseException;

	SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5);


}
