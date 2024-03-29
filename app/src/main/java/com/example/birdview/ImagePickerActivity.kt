package com.example.birdview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ImagePickerActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Get the selected image URI
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                val imagePath = getImagePath(selectedImageUri)
                identifyBirds(imagePath)
            }
        }
    }

    private fun getImagePath(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val imagePath = cursor?.getString(columnIndex ?: 0) ?: ""
        cursor?.close()
        return imagePath
    }

    private fun identifyBirds(imagePath: String) {
        val apiKey = "AIzaSyAcnPpN2K87hgK2IIjDyqReQlIPjU41kvo"
        val apiUrl = "https://vision.googleapis.com/v1/images:annotate?key=$apiKey"

        val imageBytes = getImageBytes(imagePath)
        val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

        val jsonRequest = """
            {
                "requests": [
                    {
                        "image": {
                            "content": "$base64Image"
                        },
                        "features": [
                            {"type": "LABEL_DETECTION"}
                        ]
                    }
                ]
            }
        """.trimIndent()

        val requestBody = jsonRequest.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            val labels = parseResponse(responseData)
            labels.forEach { label ->
                println("Label: ${label.description}")
            }
        } else {
            println("Error: ${response.code} - ${response.message}")
        }
    }

    private fun getImageBytes(imagePath: String): ByteArray {
        val fileInputStream = java.io.FileInputStream(imagePath)
        return fileInputStream.readBytes()
    }

    private fun parseResponse(responseData: String?): List<LabelAnnotation> {
        return Gson().fromJson(responseData, LabelResponse::class.java)?.responses?.get(0)?.labelAnnotations
            ?: emptyList()
    }

}

data class LabelResponse(val responses: List<Response>)

data class Response(val labelAnnotations: List<LabelAnnotation>)

data class LabelAnnotation(val description: String, val score: Double)

