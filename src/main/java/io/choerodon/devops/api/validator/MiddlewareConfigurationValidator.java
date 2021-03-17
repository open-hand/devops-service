package io.choerodon.devops.api.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.choerodon.core.exception.CommonException;

public class MiddlewareConfigurationValidator {

    private static List<String> appendfsyncValueList;

    private static List<String> yesOrNoList;

    private static List<String> memoryPolicyValueList;

    private static Map<String, Validator> redisValueValidatorMap;

    static {
        appendfsyncValueList = new ArrayList<>();
        appendfsyncValueList.add("no");
        appendfsyncValueList.add("always");
        appendfsyncValueList.add("everysec");
        yesOrNoList = new ArrayList<>();
        yesOrNoList.add("yes");
        yesOrNoList.add("no");
        memoryPolicyValueList = new ArrayList<>();
        memoryPolicyValueList.add("volatile-lru");
        memoryPolicyValueList.add("allkeys-lru");
        memoryPolicyValueList.add("volatile-lfu");
        memoryPolicyValueList.add("allkeys-lfu");
        memoryPolicyValueList.add("volatile-random");
        memoryPolicyValueList.add("allkeys-random");
        memoryPolicyValueList.add("volatile-ttl");
        memoryPolicyValueList.add("noeviction");

        redisValueValidatorMap = new HashMap<>();
        redisValueValidatorMap.put("appendfsync", MiddlewareConfigurationValidator::appendfsyncValidator);
        redisValueValidatorMap.put("appendonly", MiddlewareConfigurationValidator::appendonlyValidator);
        redisValueValidatorMap.put("master-read-only", MiddlewareConfigurationValidator::masterReadOnlyValidator);
        redisValueValidatorMap.put("client-output-buffer-limit-slave-soft-seconds", MiddlewareConfigurationValidator::clientOutputBufferLimitSlaveSoftSecondsValidator);
        redisValueValidatorMap.put("client-output-buffer-slave-hard-limit", MiddlewareConfigurationValidator::clientOutputBufferSlaveHardLimitValidator);
        redisValueValidatorMap.put("client-output-buffer-slave-soft-limit", MiddlewareConfigurationValidator::clientOutputBufferSlaveSoftLimitValidator);
        redisValueValidatorMap.put("hash-max-ziplist-entries", MiddlewareConfigurationValidator::hashMaxZiplistEntriesValidator);
        redisValueValidatorMap.put("hash-max-ziplist-value", MiddlewareConfigurationValidator::hashMaxZiplistValueValidator);
        redisValueValidatorMap.put("latency-monitor-threshold", MiddlewareConfigurationValidator::latencyMonitorThresholdValidator);
        redisValueValidatorMap.put("lua-time-limit", MiddlewareConfigurationValidator::luaTimeLimitValidator);
        redisValueValidatorMap.put("maxclients", MiddlewareConfigurationValidator::maxClientsValidator);
        redisValueValidatorMap.put("maxmemory-policy", MiddlewareConfigurationValidator::maxmemoryPolicyValidator);
        redisValueValidatorMap.put("proto-max-bulk-len", MiddlewareConfigurationValidator::protoMaxBulkLenValidator);
        redisValueValidatorMap.put("repl-backlog-size", MiddlewareConfigurationValidator::replBacklogSizeValidator);
        redisValueValidatorMap.put("repl-backlog-ttl", MiddlewareConfigurationValidator::replBacklogTtlValidator);
        redisValueValidatorMap.put("repl-timeout", MiddlewareConfigurationValidator::replTimeoutValidator);
        redisValueValidatorMap.put("set-max-intset-entries", MiddlewareConfigurationValidator::setMaxIntsetEntriesValidator);
        redisValueValidatorMap.put("slowlog-log-slower-than", MiddlewareConfigurationValidator::slowlogLogSlowerThanValidator);
        redisValueValidatorMap.put("slowlog-max-len", MiddlewareConfigurationValidator::slowlogMaxLenValidator);
        redisValueValidatorMap.put("timeout", MiddlewareConfigurationValidator::timeoutValidator);
        redisValueValidatorMap.put("zset-max-ziplist-entries", MiddlewareConfigurationValidator::zsetMaxZiplistEntries);
        redisValueValidatorMap.put("zset-max-ziplist-value", MiddlewareConfigurationValidator::zsetMaxZiplistValue);
    }

    public static void validateRedisConfiguration(Map<String, String> configuration) {
        configuration.forEach((k, v) -> {
            Validator validator = redisValueValidatorMap.get(k);
            if (validator != null) {
                validator.validate(v);
            }
        });
    }

    private static void appendfsyncValidator(String value) {
        if (!appendfsyncValueList.contains(value)) {
            throw new CommonException("error.redis.configuration.illegal", "appendfsync");
        }
    }

    private static void appendonlyValidator(String value) {
        if (!yesOrNoList.contains(value)) {
            throw new CommonException("error.redis.configuration.illegal", "appendonly");
        }
    }

    private static void masterReadOnlyValidator(String value) {
        if (!yesOrNoList.contains(value)) {
            throw new CommonException("error.redis.configuration.illegal", "master-read-only");
        }
    }

    private static void clientOutputBufferLimitSlaveSoftSecondsValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 60L, "client-output-buffer-limit-slave-soft-seconds");
    }

    private static void clientOutputBufferSlaveHardLimitValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 4294967296L, "client-output-buffer-slave-hard-limit");
    }

    private static void clientOutputBufferSlaveSoftLimitValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 4294967296L, "client-output-buffer-slave-soft-limit");
    }

    private static void hashMaxZiplistEntriesValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1L, 10000L, "hash-max-ziplist-entries");
    }

    private static void hashMaxZiplistValueValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1L, 10000L, "hash-max-ziplist-value");
    }

    private static void latencyMonitorThresholdValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 86400000L, "latency-monitor-threshold");
    }

    private static void luaTimeLimitValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 100L, 5000L, "lua-time-limit");
    }

    private static void maxClientsValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1000L, 50000L, "maxclients");
    }

    private static void maxmemoryPolicyValidator(String value) {
        if (!memoryPolicyValueList.contains(value)) {
            throw new CommonException("error.redis.configuration.illegal", "maxmemory-policy");
        }
    }

    private static void protoMaxBulkLenValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1048576L, 536870912L, "proto-max-bulk-len");
    }

    private static void replBacklogSizeValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 16384L, 1073741824L, "repl-backlog-size");
    }

    private static void replBacklogTtlValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 604800L, "repl-backlog-ttl");
    }

    private static void replTimeoutValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 30L, 3600L, "repl-timeout");
    }

    private static void setMaxIntsetEntriesValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1L, 10000L, "set-max-intset-entries");
    }

    private static void slowlogLogSlowerThanValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 1000000L, "slowlog-log-slower-than");
    }

    private static void slowlogMaxLenValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 1000L, "slowlog-max-len");
    }

    private static void timeoutValidator(String value) {
        checkRedisValueInRange(Long.parseLong(value), 0L, 7200L, "timeout");
    }

    private static void zsetMaxZiplistEntries(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1L, 10000L, "zset-max-ziplist-entries");
    }

    private static void zsetMaxZiplistValue(String value) {
        checkRedisValueInRange(Long.parseLong(value), 1L, 10000L, "zset-max-ziplist-value");
    }


    private static void checkRedisValueInRange(Long value, Long min, Long max, String key) {
        checkValueInRange(value,min,max,key,"error.redis.configuration.illegal");
    }

    private static void checkValueInRange(Long value,Long min,Long max,String key,String exceptionCode){
        if (value < min || value > max) {
            throw new CommonException(exceptionCode, key);
        }
    }

    @FunctionalInterface
    interface Validator {
        void validate(String value);
    }
}
