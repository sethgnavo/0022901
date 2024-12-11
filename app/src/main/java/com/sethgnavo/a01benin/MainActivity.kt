package com.sethgnavo.a01benin

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request permissions
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                ), 1
            )
        }

        // Set up the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
    }

    private fun hasPermissions(): Boolean {
        val readContacts =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        val writeContacts =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
        return readContacts == PackageManager.PERMISSION_GRANTED && writeContacts == PackageManager.PERMISSION_GRANTED
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            //processContacts()
        } else {
            Toast.makeText(this, "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
        }
    }
}