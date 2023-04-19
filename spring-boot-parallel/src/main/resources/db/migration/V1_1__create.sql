create table product
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

CREATE INDEX ON product (country) STORING (inventory,name,price);

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'US'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'UK'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'BE'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'DE'

from generate_series(1, 500000) as i;
insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'SE'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'FI'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'FR'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'NO'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'DK'
from generate_series(1, 500000) as i;

insert into product (inventory,name,price,sku,country)
select 10 + random() * 50,
       md5(random()::text),
       500.00 + random() * 500.00,
       gen_random_uuid()::text,
       'ES'
from generate_series(1, 500000) as i;

