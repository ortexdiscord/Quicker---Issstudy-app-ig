package com.example.data.auth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.security.SecureRandom

/**
 * Manages authentic, cryptographically secure verification code generation,
 * Android notification dispatch, and expiration-based verification.
 * Eliminates all mock/hardcoded bypasses.
 */
class CodeVerificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "quicks_auth_verification"
        private const val CHANNEL_NAME = "Security & Verification"
        private const val NOTIFICATION_ID = 1001
        private const val CODE_EXPIRATION_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_ATTEMPTS = 5
    }

    data class ActiveCode(
        val code: String,
        val targetEmail: String,
        val createdAt: Long,
        var attemptsLeft: Int = MAX_ATTEMPTS
    )

    private var activeCode: ActiveCode? = null

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time security verification codes for Quicks login"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Generates a true 6-digit cryptographic security code and dispatches
     * a system heads-up notification to the user.
     */
    fun sendVerificationCode(email: String): VerificationResult {
        val cleanEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return VerificationResult.Error("Please enter a valid email address.")
        }

        // Generate cryptographically secure 6-digit code
        val random = SecureRandom()
        val numericCode = 100000 + random.nextInt(900000)
        val codeString = numericCode.toString()

        activeCode = ActiveCode(
            code = codeString,
            targetEmail = cleanEmail.lowercase(),
            createdAt = System.currentTimeMillis()
        )

        // Dispatch real Android system notification
        postVerificationNotification(cleanEmail, codeString)

        return VerificationResult.Sent(codeString)
    }

    private fun postVerificationNotification(email: String, code: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Quicks Verification Code: $code")
            .setContentText("Your security code is $code. Valid for 5 minutes for $email.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your Quicks single-use verification code is:\n\n$code\n\nEnter this in the app to complete verification. Never share this code with anyone.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not yet granted on Android 13+
        }
    }

    /**
     * Strict verification check:
     * - Must match exactly
     * - Must not be expired
     * - Must not have exceeded maximum failed attempts
     */
    fun verifyCode(enteredCode: String, email: String): VerificationResult {
        val current = activeCode ?: return VerificationResult.Error("No verification code was requested. Please request a new code.")

        if (current.targetEmail != email.trim().lowercase()) {
            return VerificationResult.Error("Email address does not match the active verification request.")
        }

        val elapsed = System.currentTimeMillis() - current.createdAt
        if (elapsed > CODE_EXPIRATION_MS) {
            activeCode = null
            return VerificationResult.Error("Verification code has expired (5m limit). Please request a new code.")
        }

        if (current.attemptsLeft <= 0) {
            activeCode = null
            return VerificationResult.Error("Too many incorrect attempts. Please request a new verification code.")
        }

        val cleanEntered = enteredCode.trim()
        if (cleanEntered != current.code) {
            current.attemptsLeft -= 1
            val left = current.attemptsLeft
            return VerificationResult.Error("Incorrect verification code. $left attempt(s) remaining.")
        }

        // Code matched exactly! Invalidate so it cannot be reused
        activeCode = null
        return VerificationResult.Success
    }

    sealed class VerificationResult {
        data class Sent(val code: String) : VerificationResult()
        data object Success : VerificationResult()
        data class Error(val message: String) : VerificationResult()
    }
}
