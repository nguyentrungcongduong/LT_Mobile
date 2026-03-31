package com.gymapp.android.ui.screens.membership.event

sealed class MembershipDetailEvent {
    object Refresh : MembershipDetailEvent()
    object OnShowQrClicked : MembershipDetailEvent()
    object OnRenewClicked : MembershipDetailEvent()
}
