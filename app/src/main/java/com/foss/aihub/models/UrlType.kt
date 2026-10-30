package com.foss.aihub.models

data class LinkData(
    val url: String, val title: String, val type: LinkType = LinkType.HYPERLINK
)

enum class LinkType {
    HYPERLINK, IMAGE, EMAIL, PHONE
}