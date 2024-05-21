insert into customer (id, email, user_name, first_name, last_name)
select gen_random_uuid(),
       concat('user', i::text, '@', md5(gen_random_uuid()::text), '.com'),
       md5(gen_random_uuid()::text),
       concat('user', i::text),
       concat('user', i::text)
from generate_series(0, 1000) as i;

insert into product (id, inventory, name, price, sku)
select gen_random_uuid(),
       random()*10,
       md5(gen_random_uuid()::text),
       random() * 1000.0,
       md5(gen_random_uuid()::text)
from generate_series(0, 5000) as i;
