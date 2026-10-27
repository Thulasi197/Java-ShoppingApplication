CREATE TABLE products (
    product_id NUMBER PRIMARY KEY,
    product_name VARCHAR2(50),
    price NUMBER(10,2),
    stock NUMBER
);

CREATE TABLE orders (
    order_id NUMBER PRIMARY KEY,
    total_amount NUMBER(10,2),
    order_date DATE DEFAULT SYSDATE
);

CREATE TABLE order_items (
    order_id NUMBER,
    product_id NUMBER,
    quantity NUMBER,
    subtotal NUMBER(10,2)
);

CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 1;

INSERT INTO products VALUES (1, 'Mouse', 500, 10);
INSERT INTO products VALUES (2, 'Keyboard', 1000, 8);
INSERT INTO products VALUES (3, 'Headphones', 1500, 5);
INSERT INTO products VALUES (4, 'USB Cable', 200, 20);

COMMIT;