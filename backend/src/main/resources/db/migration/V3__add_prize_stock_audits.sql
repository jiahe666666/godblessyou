CREATE TABLE `prize_stock_audits` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `prize_id` BIGINT NOT NULL,
  `operator_user_id` BIGINT DEFAULT NULL,
  `change_type` VARCHAR(32) NOT NULL,
  `delta` INT NOT NULL,
  `stock_before` INT NOT NULL,
  `stock_after` INT NOT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_prize_stock_audits_prize_id` (`prize_id`),
  KEY `idx_prize_stock_audits_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
