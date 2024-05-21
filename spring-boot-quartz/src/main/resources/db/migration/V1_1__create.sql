create table if not exists customer
(
    id         uuid         not null default gen_random_uuid(),
    email      varchar(256) not null unique,
    user_name  varchar(128) not null unique,
    first_name varchar(128),
    last_name  varchar(128),

    primary key (id)
);

create table if not exists product
(
    id        uuid           not null default gen_random_uuid(),
    inventory int            not null,
    name      varchar(128)   not null,
    price     numeric(19, 2) not null,
    sku       varchar(128)   not null unique,

    primary key (id)
);

-- alter table product
--     add constraint check_product_positive_inventory check (product.inventory >= 0);

create table if not exists purchase_order_item
(
    order_id   uuid           not null,
    product_id uuid           not null,
    quantity   int            not null,
    unit_price numeric(19, 2) not null,
    item_pos   int            not null,

    primary key (order_id, item_pos)
);

create index fk_order_item_ref_product_idx on purchase_order_item (product_id);

create type if not exists shipment_status as enum ('placed', 'confirmed', 'cancelled','delivered');

create table if not exists purchase_order
(
    id           uuid            not null default gen_random_uuid(),
    total_price  numeric(19, 2)  not null,
    tags         varchar(128)    null,
    status       shipment_status not null default 'placed',
    date_placed  timestamptz     not null default clock_timestamp(),
    date_updated timestamptz     not null default clock_timestamp(),
    customer_id  uuid            not null,

    primary key (id)
);

alter table if exists purchase_order_item
    add constraint fk_order_item_ref_product
        foreign key (product_id)
            references product;

alter table if exists purchase_order_item
    add constraint fk_order_item_ref_order
        foreign key (order_id)
            references purchase_order;

alter table if exists purchase_order
    add constraint fk_order_customer_ref_customer
        foreign key (customer_id)
            references customer;

