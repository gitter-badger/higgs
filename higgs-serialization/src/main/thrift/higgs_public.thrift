namespace java io.vilada.higgs.serialization.thrift.dto

struct TAgentResult {
    1: binary data
    2: optional string extraDataType
    3: optional binary extraData
}

struct TAgentHealthcheckRequest {
    1: string applicationName
    2: string tierName
    3: optional string instanceName
}

struct TAgentHealthcheckResult {
    1: TAgentStatus status
    2: i32 configVersion
    3: optional string agentToken
    4: optional string message
    5: optional map<string,string> data
}

enum TAgentStatus {
    OK,
    RELOAD,
    BLOCKED,
    FAILURE
}

struct TAgentInfo {
	1: string hostname
	2: string ip
	3: i32 pid
	4: string agentVersion
	5: string vmVersion
	6: string vmArguments
	7: string osName
	8: i64 startTimestamp
	11: optional list<TServerMetaData> serverMetaData
	12: optional string instanceId
    13: optional string tierId
    14: optional string appId
}

struct TServerMetaData {
    1: optional string serverName
    2: optional string serverType
    3: optional i32 port
    4: optional string protocol
}

struct TSpanBatch {
    1: list<TSpan> spans
}

struct TSpan {
    1: string operationName
    2: i64 startTime
    3: i64 finishTime
    4: optional map<string, string> spanTags
    5: optional list<TSpanLog> spanLogs
    6: TSpanContext context
}

struct TSpanLog {
    1: i64 timestamp
    2: map<string, string> fields
}

struct TSpanContext {
    1: optional string parentAgentToken
    2: string traceId
    3: string spanId
    4: string parentSpanId
    5: string spanReferer
    6: i32 index
    7: optional map<string, string> baggage
    8: optional string instanceId
    9: optional string tierId
    10: optional string appId
}
