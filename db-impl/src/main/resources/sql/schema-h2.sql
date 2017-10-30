-- Create table
create table IF NOT EXISTS block
(
  hash       VARCHAR2(100)  PRIMARY KEY,
  height     BIGINT not null,
  create_time BIGINT not null,
  script     binary(255) DEFAULT NULL
);
 -- block  index
 create unique index IF NOT EXISTS block_height_idx on block(height);