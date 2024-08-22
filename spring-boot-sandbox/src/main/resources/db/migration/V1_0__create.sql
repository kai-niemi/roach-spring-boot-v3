--
-- Products
--

create table if not exists product
(
    id        uuid           not null default gen_random_uuid(),
    version   int            not null default 0,
    inventory int            not null,
    name      varchar(128)   not null,
    price     numeric(19, 2) not null,
    sku       varchar(128)   not null unique,
    country   varchar(128)   not null,

    primary key (id)
);

-- truncate table product;

insert into product (inventory, name, price, sku, country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'SE'
from generate_series(1, 100000) as i;

--
-- JSONB
--

create table if not exists outbox
(
    id             uuid as ((payload ->> 'eventId')::UUID) stored,
    aggregate_id   uuid as ((payload ->> 'entityId')::UUID) stored,
    aggregate_type varchar(32) not null,
    payload        jsonb       not null,
    created_at     timestamptz default clock_timestamp(),
    INVERTED INDEX event_payload (payload),

    primary key (id)
);

-- ALTER TABLE outbox ADD COLUMN price DECIMAL AS ((payload->>'price')::DECIMAL) STORED;

insert into outbox (aggregate_type, payload)
select 'Product',
       jsonb_build_object(
               'eventId', gen_random_uuid(),
               'entityId', gen_random_uuid(),
               'name', gen_random_uuid()::text,
               'price', 500.00 + random() * 500.00,
               'sku', gen_random_uuid()::text,
               'inventory', 10 + random() * 50,
               'created_at', clock_timestamp()
       ) AS event
from generate_series(1, 10000) as i;

-- SELECT id, jsonb_pretty(payload) FROM outbox;
-- SELECT sum((payload->>'price')::decimal) from outbox where aggregate_type = 'Product';

