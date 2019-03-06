package org.seckill.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
	"classpath:spring/spring-service.xml"
})
public class SeckillServiceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;
	
	@Test
	public void testGetSeckillList() {
		List<Seckill> list = seckillService.getSeckillList();
		for (Seckill seckill : list) {
			logger.info("seckill={}",seckill);
		}
		logger.info("list={}",list);
	}

	@Test
	public void testGetById() {
		long id = 1000L;
		Seckill seckill = seckillService.getById(id);
		logger.debug("seckill={}",seckill);
	}

	@Test
	public void testExportSeckillUrl() {
		long id = 1001L;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer={}",exposer);
		//exposed=true, md5=24f553d6c7139f44ae2d581cdc747254, seckillId=1000, now=0, start=0, end=0]
	}

	@Test
	public void testExecuteSeckill() {
		long id = 1000L;
		long phone = 18071439427L;
		String md5 = "24f553d6c7139f44ae2d581cdc747254";
		try {
			SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
			logger.info("seckillExecution={}",seckillExecution);
		}catch (Exception e){
			logger.info("Exception={}",e);
		}

	}

	@Test
	public void testExecuteSeckillProcedure(){
		long seckillId = 1001;
		long phone = 1807143932;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if (exposer.isExposed()){
			String md5 = exposer.getMd5();

			SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
			logger.info("execution:"+execution.getStateInfo());
		}


	}

}
