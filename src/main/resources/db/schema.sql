CREATE TABLE `user`
(
    `id`           LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `user_id`      UUID UNIQUE                     NOT NULL,
    `first_name`   VARCHAR(255)                    NOT NULL,
    `last_name`    VARCHAR(255)                    NOT NULL,
    `birthdate`    DATE                            NOT NULL,
    `password`     VARCHAR(255)                    NOT NULL,
    `created`      DATETIME                        NOT NULL,
    `last_updated` DATETIME                        NOT NULL
);
CREATE TABLE `account`
(
    `id`           LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `name`         VARCHAR(255)                    NOT NULL,
    `account_id`   UUID UNIQUE                     NOT NULL,
    `balance`      DOUBLE                          NOT NULL,
    `dispo`        DOUBLE                          NOT NULL,
    `limit`        DOUBLE                          NOT NULL,
    `created`      DATETIME                        NOT NULL,
    `last_updated` DATETIME                        NOT NULL,
    `user_id`      LONG,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE cascade ON DELETE SET NULL
);
CREATE TABLE `transaction`
(
    `id`             LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `transaction_id` UUID UNIQUE                     NOT NULL,
    `origin`         UUID                            NOT NULL,
    `target`         UUID                            NOT NULL,
    `amount`         LONG                            NOT NULL,
    `created`        DATETIME                        NOT NULL,
    FOREIGN KEY (`origin`) REFERENCES `account` (`account_id`) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (`target`) REFERENCES `account` (`account_id`) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE TABLE `administrator`
(
    `id`       LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `admin_id` UUID                            NOT NULL,
    `name`     VARCHAR(255)                    NOT NULL,
    `password` VARCHAR(255)                    NOT NULL
);