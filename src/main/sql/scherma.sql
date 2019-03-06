CREATE DATABASE seckill;

use seckill;

CREATE TABLE seckill(
seckill_id bigint NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
name varchar(120) NOT NULL COMMENT '商品名称',
number int NOT NULL COMMENT '库存数量',
start_time timestamp NOT NULL COMMENT '秒杀开启时间',
end_time timestamp NOT NULL COMMENT '秒杀结束时间',
create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)engine=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT="秒杀库存表";

insert into seckill(name,number,start_time,end_time)
values
('1000元秒杀iPad',100,'2018-11-28 00:00:00','2018-12-01 00:00:00'),
('500元秒杀小米',200,'2018-11-28 00:00:00','2018-12-01 00:00:00'),
('300元秒杀华为',300,'2018-11-28 00:00:00','2018-12-01 00:00:00'),
('200元秒杀红米',400,'2018-11-28 00:00:00','2018-12-01 00:00:00');

create table success_killed(
seckill_id bigint NOT NULL comment '秒杀商品id',
user_phone bigint NOT NULL comment '用户手机号',
state tinyint NOT NULL DEFAULT -1 comment '状态标示：-1：无效；0：成功；1：已付款',
create_time timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL comment '创建时间',
PRIMARY KEY(seckill_id,user_phone),
KEY idx_create_time(create_time)
)engine=InnoDB  DEFAULT CHARSET=utf8 COMMENT="秒杀成功明细表";



