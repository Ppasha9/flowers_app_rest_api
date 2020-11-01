CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "name" varchar,
  "email" email,
  "register_date" date,
  "sex" varchar,
  "shipping_address" varchar
);

CREATE TABLE "cart_items" (
  "cart_id" int,
  "product_id" int,
  "quantity" int DEFAULT 1,
  PRIMARY KEY ("cart_id", "product_id")
);

CREATE TABLE "cart" (
  "id" SERIAL PRIMARY KEY,
  "date" date,
  "full_price" double,
  "user" int
);

CREATE TABLE "products" (
  "id" SERIAL PRIMARY KEY,
  "name" varchar,
  "description" text,
  "price" double,
  "tags" varchar[],
  "add_date" date
);

ALTER TABLE "cart_items" ADD FOREIGN KEY ("cart_id") REFERENCES "cart" ("id");

ALTER TABLE "cart_items" ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id");

ALTER TABLE "cart" ADD FOREIGN KEY ("user") REFERENCES "users" ("id");

