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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private lateinit var btnUpdateContacts: Button

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

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        btnUpdateContacts = findViewById(R.id.btnUpdateContacts)

        var contacts = fetchContactsWith229().sortedBy { it.name } // Sort contacts by name
        adapter = ContactsAdapter(contacts)
        recyclerView.adapter = adapter

        btnUpdateContacts.setOnClickListener {
            updateContacts()
            contacts = fetchContactsWith229().sortedBy { it.name } // Re-fetch updated contacts
            adapter = ContactsAdapter(contacts) // Update adapter with new data
            recyclerView.adapter = adapter // Set the updated adapter
        }
    }

    private fun fetchContactsWith229(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val contactMap = mutableMapOf<String, MutableList<String>>()

            do {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                if (number.startsWith("+229")) {
                    contactMap.putIfAbsent(name, mutableListOf())
                    contactMap[name]?.add(number)
                }
            } while (cursor.moveToNext())

            cursor.close()

            for ((name, numbers) in contactMap) {
                contacts.add(Contact(name, numbers))
            }
        }
        return contacts
    }

    private fun hasPermissions(): Boolean {
        val readContacts =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        val writeContacts =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
        return readContacts == PackageManager.PERMISSION_GRANTED && writeContacts == PackageManager.PERMISSION_GRANTED
    }

    private fun updateContacts() {
        val resolver = contentResolver
        val ops = ArrayList<android.content.ContentProviderOperation>()
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                if (number .startsWith("+229")||number.startsWith("00229")) {
                    val newNumber = "+229 01" + number.substring(4)
                    ops.add(
                        android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build()
                    )

                    // Apply batch every 400 operations to prevent exceeding the limiT. There is
                    // a limit to the nunumber of operations per batch
                    if (ops.size >= 400) {
                        try {
                            resolver.applyBatch(ContactsContract.AUTHORITY, ops)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        ops.clear()
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        }

        // Apply remaining operations
        if (ops.isNotEmpty()) {
            try {
                resolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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