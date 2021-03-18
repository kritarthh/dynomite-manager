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

CREATE TABLE "dyno_bootstrap"."locks"(
	"key" text,
	"instanceid" text,
	PRIMARY KEY(("key", "instanceid")))
WITH CUSTOM_PROPERTIES = {
	'capacity_mode':{'throughput_mode':'PAY_PER_REQUEST'},
	'point_in_time_recovery':{'status':'enabled'}
}
