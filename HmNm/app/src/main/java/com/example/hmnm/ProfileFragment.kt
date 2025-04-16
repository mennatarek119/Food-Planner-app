package com.example.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hmnm.R
import com.example.hmnm.SharedPrefManager
import com.example.hmnm.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvEditProfile = view.findViewById<TextView>(R.id.tvEditProfile)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val logoutLayout = view.findViewById<LinearLayout>(R.id.layoutLogout)

        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let {
            val userName = it.displayName
            tvUsername.text = userName ?: "User Name"
        }

        tvEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        logoutLayout.setOnClickListener {
            showLogoutDialog(requireContext())
        }

    }
    fun logOutUser(context: Context) {
        FirebaseAuth.getInstance().signOut()
        SharedPrefManager.clearPrefs(context)
        val intent = Intent(context, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
    fun showLogoutDialog(context: Context) {
        val title = SpannableString("Confirm logout")
        title.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.teal_700)), 0, title.length, 0)

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                logOutUser(context)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.red))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        dialog.show()
    }




}
