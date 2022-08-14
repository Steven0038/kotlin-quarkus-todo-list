package com.steven.model.vo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RowDataSetVO<T : Any>(

    var list: List<T>? = null,

    var page: Int? = null,

    var show: Int? = null,

    var total: Long? = null

)
