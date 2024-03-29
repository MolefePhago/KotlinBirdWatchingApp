package com.example.birdview

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.birdview.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        progressBar = ProgressDialog(this)
        progressBar.setTitle("Registering Profile")
        progressBar.setCanceledOnTouchOutside(false)
        /*
        *Login And SignUp using Firebase in Kotlin (Android Studio 2022)
        * This code was sourced from youTube
        * CodingSTUFF
        * https://www.youtube.com/@codingstuff070
        * https://youtu.be/idbxxkF1l6k?si=QsINtb_DWBTVW0G8
        */
        binding.btnRegister.setOnClickListener {
            val fullname = binding.etFullname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateRegisterInput(fullname, email, username, password, confirmPassword)) {
                // Perform registration here
                registerUser()
                startActivity(Intent(this, Login::class.java))
            }
        }

        binding.tvHaveAccount.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private var fullName =""
    private var email =""
    private var username =""
    private var password =""
    private fun registerUser() {
        progressBar.setMessage("Creating User Account")
        progressBar.show()
        fullName = binding.etFullname.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        username = binding.etUsername.text.toString().trim()
        password= binding.etPassword.text.toString().trim()
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            userData()
        }.addOnFailureListener {
            progressBar.dismiss()
            Toast.makeText(this,"User not registered process failed",Toast.LENGTH_SHORT).show()

        }
    }
    private fun userData() {
        progressBar.setMessage("Registering User..")
        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid
        val hashMap : HashMap<String,Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["name"] = fullName
        hashMap["email"] = email
        hashMap["username"] = username
        hashMap["timestamp"] = timestamp

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(uid!!).setValue(hashMap).addOnSuccessListener {
            progressBar.dismiss()
            Toast.makeText(this,"Account created",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
        }.addOnFailureListener {
            progressBar.dismiss()
            Toast.makeText(this,"Did not save due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }


    private fun validateRegisterInput(
        fullname: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullname.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (confirmPassword.isEmpty() || confirmPassword != password) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}