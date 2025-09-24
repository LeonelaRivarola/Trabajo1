package com.example.trabajo1.ui.config

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.trabajo1.R

class ConfigFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val fontPref = findPreference<androidx.preference.ListPreference>("font_size")

        fontPref?.setOnPreferenceChangeListener { _, _ ->
            requireActivity().recreate() // reinicia la activity para aplicar el cambio
            true
        }
    }
}