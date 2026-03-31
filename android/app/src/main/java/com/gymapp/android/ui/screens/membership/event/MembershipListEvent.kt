package com.gymapp.android.ui.screens.membership.event

sealed class MembershipListEvent {
    object Refresh : MembershipListEvent()
    data class OnFilterChanged(val filter: String) : MembershipListEvent()
    data class OnPackageClicked(val planId: String) : MembershipListEvent()
}
