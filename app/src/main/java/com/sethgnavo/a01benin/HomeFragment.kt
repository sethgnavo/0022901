package com.sethgnavo.a01benin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
            findNavController().navigate(R.id.action_homeFragment_to_withCountryCodeFragment)
        }
        buttonWithoutCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_withoutCountryCodeFragment)
        }

        return view
    }
}