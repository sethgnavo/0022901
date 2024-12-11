package com.sethgnavo.a01benin

import android.content.ContentProviderOperation
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils.formatNumber
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WithoutCountryCodeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private lateinit var btnUpdateContacts: Button
    private lateinit var btnDeleteLegacyContacts: Button
    val prefixes = listOf(
        "20",//Zone géographique Ouémé, Plateau
        "21",//Zone géographique Littoral, Atlantique
        "22",//Zone géographique Mono, Couffo, Zou, Collines
        "23",//Zone géographique Atacora, Donga, Alibori, Borgou

        "40",//Celtiis
        "41",//Celtiis
        "42",//MTN
        "43",//Celtiis
        "44",//Celtiis
        "46",//MTN

        "50",//MTN
        "51",//MTN
        "52",//MTN
        "53",//MTN
        "54",//MTN
        "55",//Moov
        "56",//MTN
        "57",//MTN
        "58",//Moov
        "59",//MTN

        "60",//Moov
        "61",//MTN
        "62",//MTN
        "63",//Moov
        "64",//Moov
        "65",//Moov
        "66",//MTN
        "67",//MTN
        "68",//Moov
        "69",//MTN

        "80",//Numéros longs d’accès aux services (serveur internet, prépaiement videotext…)
        "81",//Réservé pour services à valeur ajoutée (numéros verts, azur…)
        "85",//Prestataires de services (dont «opérateurs virtuels»)

        "90",//MTN
        "91",//MTN
        "94",//Moov
        "95",//Moov
        "96",//MTN
        "97",//MTN
        "98",//Moov
        "99"//Moov
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_without_country_code, container, false)

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

        var contacts =
            fetchContactsWithoutCountryCode().sortedBy { it.name.lowercase() } // Sort contacts by name
        adapter = ContactsAdapter(contacts)
        recyclerView.adapter = adapter

        btnUpdateContacts.setOnClickListener {
            updateNoCountryCodeLegacyContacts()
            adapter.updateContacts(fetchContactsWithoutCountryCode()) // Update adapter with new data
        }

        btnDeleteLegacyContacts.setOnClickListener {
            deleteLegacyContacts()
            adapter.updateContacts(fetchContactsWithoutCountryCode()) // Update adapter with new data
        }

        return view
    }


    private fun fetchContactsWithoutCountryCode(): List<Contact> {
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

                if (!cleanedNumber.startsWith("+229") &&
                    !cleanedNumber.startsWith("00229") && // number does not start with a country code
                    cleanedNumber.length == 8 && // number is 8 digits long
                    prefixes.any { cleanedNumber.startsWith(it) } // number has one of the prefixes of telecom operators in Benin
                ) {
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

    private fun updateNoCountryCodeLegacyContacts() {
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

        if (cursor != null && cursor.moveToFirst()) {

            do {
                val contactId =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val rawContactId = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        ContactsContract
                            .CommonDataKinds.Phone.RAW_CONTACT_ID
                    )
                )
                val number =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val cleanedNumber = number.replace(" ", "").replace("-", "")

                // Check if the number needs to be updated
                if (!cleanedNumber.startsWith("+229") &&
                    !cleanedNumber.startsWith("00229") && // number does not start with a country code
                    cleanedNumber.length == 8 && // number is 8 digits long
                    prefixes.any { cleanedNumber.startsWith(it) } // number has one of the prefixes of telecom operators in Benin
                ) {
                    // Check if a number with the updated format already exists for this contact
                    val updatedCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ? AND ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
                        arrayOf(contactId, "+229 01%"), null
                    )

                    val alreadyUpdated = updatedCursor?.use { it.count > 0 } ?: false

                    if (!alreadyUpdated) {
                        val newNumber = "+229 01 " + number.getLast8Digits().formatNumber()

                        Log.d(
                            "NUMBER UPDATE",
                            "Old: " + number + " New: " + newNumber
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

    private fun deleteLegacyContacts() {
        val resolver = requireContext().contentResolver
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
                if (!number.startsWith("+") && !number.startsWith("00")//number does not start with a country code
                    && number.replace(" ", "").replace("-", "").length == 8//number is 8 digits long
                    && prefixes.any { number.startsWith(it) } //number has one of the prefixes of telecom operators in Benin
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