package dev.drzepka.pvstats.model.grafana

class SearchRequest {
    var type = ""
    var target = ""
}

class SearchResponse(vararg targets: String) : ArrayList<String>(targets.toList())