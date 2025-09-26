package com.example.oblivion

import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class MainSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_main_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileJson = RustBridge.getCurrentProfile()
        val type = object : TypeToken<Profile>() {}.type
        val profile: Profile = Gson().fromJson(profileJson, type)

        val qrImageView = view.findViewById<ImageView>(R.id.qrCodeImage)
        val usernameView = view.findViewById<TextView>(R.id.userNameText)
        val userIdView = view.findViewById<TextView>(R.id.userIdText)
        val goBackArrow = view.findViewById<ImageView>(R.id.arrowBackIcon)

        usernameView.text = profile.username
        userIdView.text = profile.user_id
        val firstLetter = profile.username.firstOrNull()?.uppercaseChar() ?: '?'

        val qrData = "oblivion://add?user=${profile.user_id}&name=${profile.username}"
        qrImageView.setImageBitmap(generateQrWithLetter(qrData, firstLetter))

        goBackArrow.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** QR code with circular letter overlay */
    private fun generateQrWithLetter(data: String, letter: Char): Bitmap {
        val size = 1024   // high-res for clarity
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,                      // small border
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H  // highest level
        )

        val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        val qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                qrBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.WHITE else Color.BLACK)
            }
        }

        // Create overlay
        val overlaySize = (size * 0.22f).toInt()
        val overlay = createLetterBitmap(letter, overlaySize)

        // Combine
        val combined = qrBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(combined)
        val left = (combined.width - overlay.width) / 2f
        val top = (combined.height - overlay.height) / 2f
        canvas.drawBitmap(overlay, left, top, null)

        return combined
    }

    /** Circular badge with letter */
    /** Circular badge with letter and gradient background */
    private fun createLetterBitmap(letter: Char, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Gradient from dark purple to black
        val gradient = LinearGradient(
            0f, 0f, 0f, size.toFloat(),
            Color.parseColor("#4B0082"),  // dark purple
            Color.BLACK,
            Shader.TileMode.CLAMP
        )

        val bg = Paint().apply {
            isAntiAlias = true
            shader = gradient
        }

        // Draw rounded circle background
        val radius = size / 2f
        c.drawRoundRect(
            0f, 0f, size.toFloat(), size.toFloat(),
            radius, radius, bg
        )

        // Draw letter on top
        val txt = Paint().apply {
            color = Color.WHITE  // better contrast on dark background
            isAntiAlias = true
            textSize = size * 0.55f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val y = size / 2f - (txt.descent() + txt.ascent()) / 2
        c.drawText(letter.toString(), size / 2f, y, txt)

        return bmp
    }

}
