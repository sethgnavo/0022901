package com.sethgnavo.a01benin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(private var contacts: List<Contact>) :
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

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts.sortedBy { it.name.lowercase() }
        notifyDataSetChanged()
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.contact_name)
        private val numbersTextView: TextView = itemView.findViewById(R.id.contact_numbers)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.contact_avatar)

        fun bind(contact: Contact) {
            nameTextView.text = contact.name

            val initials = getInitials(contact.name)
            avatarImageView.setImageBitmap(createAvatarBitmap(initials))

            val spannableString = SpannableStringBuilder()

            contact.numbers.forEachIndexed { index, number ->
                if (number.replace(" ", "").replace("-", "").startsWith("+22901") ||
                    number.replace(" ", "").replace("-", "").startsWith("0022901")
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

        private fun createAvatarBitmap(letters: String): Bitmap {
            val size = 100 // Size of the bitmap
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
                color = getRandomColor()
                style = Paint.Style.FILL
            }

            // Draw a circle
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

            // Draw the letters
            paint.color = Color.WHITE // Text color
            paint.textSize = 40f // Text size
            paint.textAlign = Paint.Align.CENTER

            // Calculate the position to center the text
            val xPos = size / 2f
            val yPos = (size / 2f - (paint.descent() + paint.ascent()) / 2)

            canvas.drawText(letters, xPos, yPos, paint)

            return bitmap
        }

        private fun getRandomColor(): Int {
            val random = java.util.Random()
            return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }

        private fun getInitials(name: String): String {
            val words = name.split(" ")
            return when {
                words.size > 1 -> "${words[0].firstOrNull() ?: ""}${words[1].firstOrNull() ?: ""}"
                words.isNotEmpty() -> words[0].firstOrNull()?.toString() ?: ""
                else -> ""
            }
        }
    }
}