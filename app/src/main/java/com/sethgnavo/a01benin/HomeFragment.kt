package com.sethgnavo.a01benin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Set up button click listener to navigate to OtherFragment
        val buttonWithCountryCode = view.findViewById<Button>(R.id.buttonNavigateToWithCountryCode)
        val buttonWithoutCountryCode = view.findViewById<Button>(
            R.id.buttonNavigateToWithoutCountryCode
        )
        buttonWithCountryCode.setOnClickListener {
            if (hasPermissions())
                findNavController().navigate(R.id.action_homeFragment_to_withCountryCodeFragment)
            else
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.contact_permissions_are_required_to_proceed),
                    Toast.LENGTH_LONG
                ).show()
        }
        buttonWithoutCountryCode.setOnClickListener {
            if (hasPermissions())
                findNavController().navigate(R.id.action_homeFragment_to_withoutCountryCodeFragment)
            else
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.contact_permissions_are_required_to_proceed),
                    Toast.LENGTH_LONG
                ).show()
        }

        return view
    }

    private fun hasPermissions(): Boolean {
        val readContacts =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS)
        val writeContacts =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_CONTACTS)
        return readContacts == PackageManager.PERMISSION_GRANTED && writeContacts == PackageManager.PERMISSION_GRANTED
    }
}