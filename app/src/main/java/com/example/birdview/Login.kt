package com.example.birdview

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.birdview.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressDialog
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        progressBar = ProgressDialog(this)
        progressBar.setTitle("Logging in")
        progressBar.setCanceledOnTouchOutside(false)
        /*
        *Login And SignUp using Firebase in Kotlin (Android Studio 2022)
        * This code was sourced from youTube
        * CodingSTUFF
        * https://www.youtube.com/@codingstuff070
        * https://youtu.be/idbxxkF1l6k?si=QsINtb_DWBTVW0G8*/

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateLoginInput(email, password)) {
                // Perform login here
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        progressBar.dismiss()
                        startActivity(Intent(this, HomePage::class.java))

                    }else{
                        progressBar.dismiss()
                        Toast.makeText(this,"Email or Password is Wrong",Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        binding.tvHaventAccount.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }
    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}