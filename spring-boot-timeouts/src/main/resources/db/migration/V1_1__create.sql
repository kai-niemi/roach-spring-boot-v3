create table order_items
(
    order_id   uuid           not null,
    product_id uuid           not null,
    quantity   int            not null,
    unit_price numeric(19, 2) not null,
    item_pos   int            not null,

    primary key (order_id, item_pos)
);

create type shipment_status as enum ('placed', 'confirmed', 'cancelled','delivered');

create table orders
(
    id           uuid            not null default gen_random_uuid(),
    total_price  numeric(19, 2)  not null,
    tags         varchar(128)    null,
    status       shipment_status not null default 'placed',
    date_placed  timestamptz     not null default clock_timestamp(),
    date_updated timestamptz     not null default clock_timestamp(),

    primary key (id)
);

create table products
(
    id        uuid           not null default gen_random_uuid(),
    inventory int            not null,
    name      varchar(128)   not null,
    price     numeric(19, 2) not null,
    sku       varchar(128)   not null unique,

    primary key (id)
);

alter table products
    add constraint check_product_positive_inventory check (products.inventory >= 0);

alter table if exists order_items
    add constraint fk_order_item_ref_product
        foreign key (product_id)
            references products;

alter table if exists order_items
    add constraint fk_order_item_ref_order
        foreign key (order_id)
            references orders;

-- Foreign key indexes
create index fk_order_item_ref_product_idx on order_items (product_id);