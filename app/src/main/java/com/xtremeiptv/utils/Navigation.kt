package com.xtremeiptv.utils

sealed class Screen(val route: String) {
    object ProfileSelection : Screen("profile_selection")
    object ProfileAddEdit : Screen("profile_add_edit/{profileId}") {
        fun pass(profileId: String? = null) = "profile_add_edit/${profileId ?: "new"}"
    }
    object Main : Screen("main")
    object Player : Screen("player/{contentId}/{contentType}/{title}") {
        fun pass(contentId: String, contentType: String, title: String) = "player/$contentId/$contentType/$title"
    }
    object AccountInfo : Screen("account_info")
    object Search : Screen("search")
    object Settings : Screen("settings")
}

sealed class Tab(val route: String, val title: String) {
    object Live : Tab("live", "Live")
    object Movies : Tab("movies", "Movies")
    object Series : Tab("series", "Series")
}
