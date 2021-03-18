CREATE KEYSPACE dyno_bootstrap WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE "dyno_bootstrap"."tokens"(
	"key" text,
	"id" text,
	"appid" text,
	"availabilityzone" text,
	"datacenter" text,
	"elasticip" text,
	"hostname" text,
	"instanceid" text,
	"location" text,
	"token" text,
	"updatetime" timeuuid,
	PRIMARY KEY("key"))
WITH CUSTOM_PROPERTIES = {
	'capacity_mode':{'throughput_mode':'PAY_PER_REQUEST'},
	'point_in_time_recovery':{'status':'enabled'}
};

CREATE TABLE dyno_bootstrap.locks (
    key blob,
    instanceid text,
    PRIMARY KEY (key)
) WITH COMPACT STORAGE
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys':'ALL', 'rows_per_partition':'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.SnappyCompressor'}
    AND dclocal_read_repair_chance = 0.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 256
    AND read_repair_chance = 1.0
    AND speculative_retry = 'NONE';
