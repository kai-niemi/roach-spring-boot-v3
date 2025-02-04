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
    country   varchar(128)   not null default 'SE',

    primary key (id)
);

-- truncate table product;

-- insert into product (inventory, name, price, sku, country)
-- select 10 + random() * 50,
--        md5(random()::text),
--        500.00 + random() * 500.00,
--        gen_random_uuid()::text,
--        'SE'
-- from generate_series(1, 100) as i;
