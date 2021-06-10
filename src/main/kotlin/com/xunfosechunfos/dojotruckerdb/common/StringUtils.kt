package com.xunfosechunfos.dojotruckerdb.common


fun String.parseToRows(): List<String> = this.split("\n")
    .filter { it.isNotBlank() }
