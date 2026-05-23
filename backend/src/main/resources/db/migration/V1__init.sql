CREATE TABLE `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(20) NOT NULL COMMENT '用户名',
  `email` VARCHAR(120) NOT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT 'bcrypt 哈希',
  `is_verified` TINYINT(1) DEFAULT 0 COMMENT '邮箱是否验证',
  `is_admin` TINYINT(1) DEFAULT 0,
  `last_lottery_at` DATETIME COMMENT '最后抽奖时间',
  `daily_lottery_count` INT DEFAULT 0 COMMENT '今日已抽次数',
  `last_lottery_date` DATE COMMENT '最后抽奖日期',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `prizes` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '奖品名称',
  `description` TEXT COMMENT '描述',
  `prize_type` ENUM('code','image') NOT NULL COMMENT '类型：兑换码/图片',
  `stock_total` INT DEFAULT 0 COMMENT '总库存',
  `stock_left` INT DEFAULT 0 COMMENT '剩余库存',
  `probability` DECIMAL(10,8) NOT NULL DEFAULT 0.00000000 COMMENT '概率权重',
  `enabled` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `prize_items` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `prize_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL COMMENT '兑换码文本或图片URL',
  `is_used` TINYINT(1) DEFAULT 0,
  `used_by` BIGINT DEFAULT NULL,
  `used_at` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_prize_id` (`prize_id`),
  KEY `idx_is_used` (`is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lottery_records` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `prize_id` BIGINT NOT NULL,
  `prize_item_id` BIGINT COMMENT '具体发放的奖品项ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `email_verifications` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token` VARCHAR(128) NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `used` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
