CREATE  KEYSPACE IF NOT EXISTS feed_migrator
    WITH REPLICATION = {
        'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS feed_migrator.accounts (
    chat_id        bigint,
    social_media   varchar,
    account_id     bigint,
    user_full_name varchar,
    access_token   varchar,
    refresh_token  varchar,
    PRIMARY KEY ((chat_id), social_media, account_id)
);

CREATE TABLE IF NOT EXISTS feed_migrator.channel_groups (
    channel_id   bigint,
    social_media varchar,
    group_id     bigint,
    group_name   varchar,
    access_token varchar,
    chat_id      bigint,
    account_id   bigint,
    PRIMARY KEY ((channel_id), social_media, group_id)
);
