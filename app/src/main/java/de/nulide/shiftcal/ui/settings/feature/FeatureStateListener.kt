package de.nulide.shiftcal.ui.settings.feature

interface FeatureStateListener {
    fun onFeatureStateChanged(state: Feature.Companion.STATE)
}