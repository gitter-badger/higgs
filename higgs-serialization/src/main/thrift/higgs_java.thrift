namespace java io.vilada.higgs.serialization.thrift.dto

struct TAgentStatBatch {
    1: list<TAgentStat> agentStats
}

struct TAgentStat {
    1: i64 timestamp
    2: i64 collectInterval
    3: optional i64 loadedClassCount
    4: optional i64 unloadedClassCount
    5: optional i64 totalThreadCount
    6: optional i64 activeThreadCount
    7: optional TJvmMemory memory
    8: optional list<TJvmGc> gc
    9: optional string instanceId
    10: optional string tierId
    11: optional string appId
}

struct TJvmMemory {
    1: i64 heapUsed
    2: i64 heapCommitted
    3: i64 heapMax
    4: i64 nonHeapUsed
    5: i64 nonHeapCommitted
    6: i64 nonHeapMax
    8: list<TJvmMemoryDetail> jvmMemoryDetail
}

struct TJvmMemoryDetail {
    1: string area
    2: string manager
    3: i64 used
    4: i64 committed
    5: i64 max
    6: string areaType
}

enum TJvmGcType {
    UNKNOWN,
    SERIAL_NEW,
    SERIAL_OLD,
    PARNEW_NEW,
    PARALLEL_SCAVENGE_NEW,
    PARALLEL_SCAVENGE_OLD,
    CMS_OLD,
    G1_NEW,
    G1_OLD,
    UNKNOWN_NEW,
    UNKNOWN_OLD
}

enum TJvmGCArea {
    NEW,
    OLD,
    UNKNOWN
}

struct TJvmGc {
    1: string collector
    2: TJvmGcType type = TJvmGcType.UNKNOWN
    3: i64 gcCount
    4: i64 gcTime
    5: TJvmGCArea area = TJvmGCArea.UNKNOWN
}

struct TThreadDumpRequest {
    1: i64 agentThreadDumpId
    2: string agentToken
    3: i64 dumpInterval
    4: TThreadDumpStatus status
}

enum TThreadDumpStatus {
    PROCESSING,
    CANCELED
}

struct TThreadDumpBatch {
    1: i64 agentThreadDumpId
    2: i64 startTimestamp
    3: i64 interval
    4: optional list<TThreadDump> threadDumps
    5: optional string instanceId
    6: optional string tierId
    7: optional string appId
}

struct TThreadDump {
    1: i64 id
    2: string name
    3: TThreadState state
    4: i64 cpuTime
    5: i64 blockedTime
    6: i64 blockedCount
    7: i64 waitedTime
    8: i64 waitedCount
    9: bool isInNative
    10: optional list<TThreadStackTrace> stackTrace
}

enum TThreadState {
    NEW,
    RUNNABLE,
    BLOCKED,
    WAITING,
    TIMED_WAITING,
    TERMINATED
}

struct TThreadStackTrace {
    1: string className
    2: string methodName
    3: i32 lineNumber
}
