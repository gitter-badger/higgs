namespace java io.vilada.higgs.serialization.thrift.dto

struct TWebAgentLoadBatch {
    1: list<TWebAgentLoad> loadBatch
}

struct TWebAgentLoad {
    1: optional string instanceId
    2: optional string tierId
    3: optional string appId
    4: string traceId
    5: string urlDomain
    6: string urlQuery
    7: i64 reportTime
    8: i64 firstScreenTime
    9: i64 whiteScreenTime
    10: i64 operableTime
    11: i64 resourceLoadedTime
    12: i64 loadedTime
    13: string browser
    14: optional map<string, string> backupProperties
    15: optional map<string, i64> backupQuota
}

struct TWebAgentAjaxBatch {
    1: list<TWebAgentAjax> ajaxBatch
}


struct TWebAgentAjax {
    1: optional string instanceId
    2: optional string tierId
    3: optional string appId
    4: string traceId
    5: string urlDomain
    6: string urlQuery
    7: i64 reportTime
    8: i64 loadedTime
    9: string browser
   10: optional map<string, string> backupProperties
   11: optional map<string, i64> backupQuota
}