create table attachment
(
    id             int          not null default unordered_unique_rowid(),
    content        bytea        null,
    content_type   varchar(15)  not null default 'application/octet-stream',
    checksum       int          not null default 0,
    created_at     timestamptz  not null default clock_timestamp(),
    name           varchar(64)  not null,
    description    varchar(256) null,
    content_length int          not null,

    primary key (id),
    family f1 (id, content_type, checksum, created_at, name, description, content_length),
    family f2 (content)
);

CREATE INDEX ON attachment (name) STORING (description,content_length,content_type,checksum);
