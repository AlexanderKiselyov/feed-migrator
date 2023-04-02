CREATE  KEYSPACE IF NOT EXISTS feed_migrator
    WITH REPLICATION = {
        'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS feed_migrator.channel_groups (
    chat_id bigint,
    channel_id bigint,
    social_network varchar,
    group_id bigint,
    access_token text,
    account_id tinyint,
    PRIMARY KEY ((chat_id), channel_id, social_network)
);

CREATE TABLE IF NOT EXISTS feed_migrator.accounts (
    chat_id bigint,
    social_network varchar,
    account_id tinyint,
    refresh_token text,
    --  доп инфа о пользователе для ui, напр. его никнейм в соцсети
    PRIMARY KEY ((chat_id), social_network, account_id)
);

CREATE TABLE IF NOT EXISTS feed_migrator.user_accounts_count(
    chat_id bigint PRIMARY KEY,
    accounts_count tinyint
);
