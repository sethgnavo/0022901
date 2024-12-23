package com.sethgnavo.a01benin

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WithCountryCodeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private lateinit var btnUpdateContacts: Button
    private lateinit var btnDeleteLegacyContacts: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_with_country_code, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        recyclerView = view.findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )

        btnUpdateContacts = view.findViewById(R.id.btnUpdateContacts)
        btnDeleteLegacyContacts = view.findViewById(R.id.btnDeleteLegacyContacts)

        val contacts =
            fetchContactsWith229().sortedBy { it.name.lowercase() } // Sort contacts by name
        adapter = ContactsAdapter(contacts)
        recyclerView.adapter = adapter

        btnUpdateContacts.setOnClickListener {

            progressBar.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE

            updateLegacyContacts()
            adapter.updateContacts(fetchContactsWith229()) // Update adapter with new data
        }

        btnDeleteLegacyContacts.setOnClickListener {
            deleteLegacyContacts()
            adapter.updateContacts(fetchContactsWith229()) // Update adapter with new data
        }

        return view
    }

    private fun fetchContactsWith229(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = requireContext().contentResolver
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
                val cleanedNumber = number.replace(" ", "").replace("-", "")

                if (cleanedNumber.startsWith("+229") || cleanedNumber.startsWith("00229")) {
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

    private fun updateLegacyContacts() {

        val resolver = requireContext().contentResolver
        val ops = ArrayList<ContentProviderOperation>()
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )

        var updatedContacts = 0

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val contactId =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val rawContactId =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID))

                val number =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val cleanedNumber = number.replace(" ", "").replace("-", "")

                if ((cleanedNumber.startsWith("+229") || cleanedNumber.startsWith("00229")) &&
                    !(cleanedNumber.startsWith("+22901") || cleanedNumber.startsWith("0022901"))
                ) {
                    val updatedCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ? AND ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
                        arrayOf(contactId, "+229 01%"), null
                    )

                    val alreadyUpdated = updatedCursor?.use { it.count > 0 } ?: false

                    if (!alreadyUpdated) {
                        val newNumber = "+229 01 " + number.getLast8Digits().formatNumber()
                        updatedContacts++
                        Log.d(
                            "NUMBER UPDATE", "Old number: $number New number: $newNumber, " +
                                    "count: $updatedContacts"
                        )
                        ops.add(
                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                .withValue(
                                    ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                                )
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                                .withValue(
                                    ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                                )
                                .build()
                        )

                        progressText.text = "$updatedContacts contacts updated"

                        if (ops.size >= 400) {
                            applyBatchSafely(resolver, ops)
                        }
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        }

        if (ops.isNotEmpty()) {
            applyBatchSafely(resolver, ops)
        }

        progressBar.visibility = View.GONE
        progressText.visibility = View.GONE
    }

    private fun applyBatchSafely(
        resolver: ContentResolver,
        ops: ArrayList<ContentProviderOperation>
    ) {
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ops.clear()
        }
    }

    private fun deleteLegacyContacts() {
        val resolver = requireActivity().contentResolver
        val ops = ArrayList<ContentProviderOperation>()
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone._ID
            ),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val contactId =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val number =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val phoneId =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID))

                // Check if the number is a legacy number
                if ((number.replace(" ", "").replace("-", "")
                        .startsWith("+229") || number.replace(" ", "").replace("-", "")
                        .startsWith("00229")) && !number.replace(" ", "").replace("-", "")
                        .startsWith("+22901")
                ) {
                    // Check if a number with the updated format exists for this contact
                    val updatedCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ? AND ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
                        arrayOf(contactId, "+229 01%"),
                        null
                    )

                    val hasUpdatedNumber = updatedCursor?.use { it.count > 0 } ?: false

                    if (hasUpdatedNumber) {
                        // Delete the legacy number
                        ops.add(
                            ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                                .withSelection("${ContactsContract.Data._ID} = ?", arrayOf(phoneId))
                                .build()
                        )

                        // Apply batch every 400 operations to prevent exceeding the limit
                        if (ops.size >= 400) {
                            try {
                                resolver.applyBatch(ContactsContract.AUTHORITY, ops)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            ops.clear()
                        }
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

}