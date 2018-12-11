CREATE TABLE IF NOT EXISTS `disk_analysis` (
	`id` Integer PRIMARY KEY NOT NULL,
	`log_time` DATETIME NOT NULL,
	`store_code` int NOT NULL,
	`esl_id` VARCHAR(32) NOT NULL,
	`session_type` VARCHAR(16) NOT NULL,
	`frames` SMALLINT NOT NULL,
	`retry_times` SMALLINT NOT NULL,
	`spent_time` int NOT NULL
);

CREATE TABLE IF NOT EXISTS `receive` (
	`id` Integer PRIMARY KEY NOT NULL,
    `store_code` INTEGER NOT NULL,
    `log_time` DATETIME NOT NULL,
    `esl_id` VARCHAR(32) NOT NULL,
    `type` VARCHAR(32) NOT NULL,
    `retry_times` INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `network_analysis` (
	`id` Integer PRIMARY KEY NOT NULL,
    `store_code` INTEGER NOT NULL,
    `log_time` DATETIME NOT NULL,
    `esl_id` VARCHAR(32) NOT NULL,
    `rf_power` INTEGER NOT NULL,
    `ap_id` INTEGER NOT NULL,
    `task_id` BIGINT NOT NULL
);