package com.sethgnavo.a01benin

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = contacts.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.contact_name)
        private val numbersTextView: TextView = itemView.findViewById(R.id.contact_numbers)

        fun bind(contact: Contact) {
            nameTextView.text = contact.name

            val spannableString = SpannableStringBuilder()

            contact.numbers.forEachIndexed { index, number ->
                if (number.startsWith("+22901") || number.startsWith("+229 01") || number.startsWith
                        ("00229") || number.startsWith("00 229")
                ) {
                    // Highlight numbers starting with +22901 in blue
                    val start = spannableString.length
                    spannableString.append(number)
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.BLUE),
                        start,
                        spannableString.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    spannableString.append(number)
                }

                // Add a separator if there are more numbers
                if (index < contact.numbers.size - 1) {
                    spannableString.append(" | ")
                }
            }

            numbersTextView.text = spannableString
        }
    }
}