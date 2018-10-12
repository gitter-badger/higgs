namespace java io.vilada.higgs.processing.flink.dto

struct AggregateInstance {
    1: string instanceId
    2: i64 timestamp
}

struct ReprocessingTrace {
    1: string traceId
    2: i32 processTimes
}