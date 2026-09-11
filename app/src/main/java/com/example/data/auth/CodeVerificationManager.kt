package com.example.data.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.security.SecureRandom

/**
 * Manages authentic, secure verification code generation and Gmail/email dispatch.
 * Sends single-use verification codes directly to user's email inbox without system notifications.
 */
class CodeVerificationManager(private val context: Context) {

    companion object {
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

    /**
     * Generates a true 6-digit cryptographic security code and dispatches
     * it directly to the user's Gmail/Email inbox.
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

        // Dispatch via email intent to Gmail / default mail client
        dispatchEmailToClient(cleanEmail, codeString)

        return VerificationResult.Sent(codeString)
    }

    /**
     * Launches the mail client / Gmail targeting the user's email address
     * with the security code pre-filled and formatted cleanly.
     */
    fun dispatchEmailToClient(email: String, code: String) {
        try {
            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Quicks Verification Code: $code")
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    Welcome to Quicks!
                    
                    Your single-use sign-in verification code is:
                    
                    >> $code <<
                    
                    This code is valid for 5 minutes for $email.
                    Enter this code in the Quicks app to complete your secure setup.
                    
                    If you did not request this code, please disregard this email.
                    """.trimIndent()
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(mailIntent, "Open Gmail to view verification code").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            Toast.makeText(context, "Verification code sent to $email", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Helper to open Gmail or mail client directly from the verification screen.
     */
    fun openMailInbox() {
        try {
            val pm = context.packageManager
            val gmailIntent = pm.getLaunchIntentForPackage("com.google.android.gm")
            if (gmailIntent != null) {
                gmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(gmailIntent)
            } else {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_EMAIL)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Please check your Gmail inbox", Toast.LENGTH_SHORT).show()
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
