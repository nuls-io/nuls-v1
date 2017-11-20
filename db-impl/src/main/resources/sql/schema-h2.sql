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

create table IF NOT EXISTS account_local
(
  id VARCHAR2(40) PRIMARY KEY,
  address VARCHAR2 (40) NOT NULL,
 pubkey BINARY  DEFAULT NULL,
 create_time BIGINT NOT NULL,
 create_height BIGINT NOT NULL,
 tx_hash BINARY  DEFAULT NULL,
 alias VARCHAR2 (100),
 version INTEGER NOT NULL,
 prikey VARCHAR(100) DEFAULT NULL,
 pri_seed BINARY DEFAULT NULL,
 EXTEND BINARY DEFAULT NULL
);
create unique index IF NOT EXISTS account_local_idx on account_local(id);
create table IF NOT EXISTS account
(
  id VARCHAR2(40) PRIMARY KEY,
  address VARCHAR2 (40) NOT NULL,
 pubkey BINARY  DEFAULT NULL,
 create_time BIGINT NOT NULL,
 create_height BIGINT NOT NULL,
 tx_hash BINARY  DEFAULT NULL,
 alias VARCHAR2 (100),
 version INTEGER NOT NULL,
 bytes BINARY DEFAULT NULL
);
create unique index IF NOT EXISTS account_idx on account(id);
