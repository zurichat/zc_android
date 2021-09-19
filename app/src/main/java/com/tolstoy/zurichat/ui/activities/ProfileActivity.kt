package com.tolstoy.zurichat.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tolstoy.zurichat.R
import com.tolstoy.zurichat.models.User
import com.tolstoy.zurichat.ui.profile.data.ProfilePayload
import com.tolstoy.zurichat.ui.profile.data.ProfileResponse
import com.tolstoy.zurichat.ui.profile.network.Constants
import com.tolstoy.zurichat.ui.profile.network.ProfileService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class ProfileActivity: AppCompatActivity() {

    private lateinit var savedName : TextView
    private lateinit var savedAbout : TextView

    //token id
    private var token: String? = null

    private var client: OkHttpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        chain.proceed(newRequest)
    }).build()

    //prepare retrofit service
    private val retrofit: Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitService: ProfileService = retrofit
        .create(ProfileService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras: Bundle? = intent.extras

        val user: User? = extras?.getParcelable("USER")

        setContentView(R.layout.activity_profile)

        savedName = findViewById(R.id.saved_name)
        savedAbout = findViewById(R.id.saved_about)


        token = user?.token

        val about = findViewById<ImageView>(R.id.edit_about)
        val camera = findViewById<ImageView>(R.id.img_camera)
        val phoneEdit = findViewById<ImageView>(R.id.imgPhone_editBT)
        val userName = findViewById<ImageView>(R.id.edit_name)

        val dialog = CreateDialog(layoutInflater,this)

        val editNameDialog = dialog.createEditNameDialog(savedName)
        val editAboutDialog = dialog.createEditAboutDialog(savedAbout)

        userName.setOnClickListener {
            editNameDialog.show()
        }

        about.setOnClickListener {
            editAboutDialog.show()
        }

        camera.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view=layoutInflater.inflate(R.layout.dialog_layout,null)
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()

            val gallery = view.findViewById<ImageView>(R.id.imageView_gallery)

            //initialize imagePicker library
            gallery.setOnClickListener {
                launcher.launch(
                    com.github.drjacky.imagepicker.ImagePicker.with(this)
                        //...
                        .galleryOnly()
                        .crop()
                        .createIntent()
                )

            }


            val cam = view.findViewById<ImageView>(R.id.imageView_cam)
            //launch camera
            cam.setOnClickListener {
                launcher.launch(
                    (
                            com.github.drjacky.imagepicker.ImagePicker.with(this)
                                .cameraOnly()
                                .crop()
                                .cropSquare()
                                .createIntent()
                            )
                )
            }


        }
        // The following lines of code creates a dialog to change the Phone Number of the
        // user.
        phoneEdit.setOnClickListener{

            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.phoneno_edittext, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_phoneno)
            val phoneTextView = findViewById<TextView>(R.id.tv_phoneno)

            with(builder){
                setTitle("Edit Phone Number")
                setPositiveButton("Save"){ _, _ ->
                    updateProfile()
                    phoneTextView.text = editText.text.toString() // populates the value of the
                    Timber.d("Update Successful") // EditText on the TextView

                    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val editor = preferences.edit()
                    editor.putString("phone", java.lang.String.valueOf(editText.text.toString()))
                    editor.apply()

                }
                setNegativeButton("Cancel") { _, _ ->
                    Timber.d("This button clicked successfully!!") //just for log purposes
                }
                setView(dialogLayout)
                show()
            }

        }
    }


    //start activity for result launcher for Image gallery
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data!!
            // Use the uri to load the image
            val profilePhoto = findViewById<ImageView>(R.id.profile_photo)

            // Saves image URI as string to Default Shared Preferences
            val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = preferences.edit()
            editor.putString("image", java.lang.String.valueOf(uri))
            editor.commit()

            //set profile photo to image uri
            profilePhoto.setImageURI(uri)
            profilePhoto.invalidate()
            Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        val profilePhoto = findViewById<ImageView>(R.id.profile_photo)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val mImageUri = preferences.getString("image", null)
        if(mImageUri != null){
            profilePhoto.setImageURI(Uri.parse(mImageUri))
        }

        val phoneTextView = findViewById<TextView>(R.id.tv_phoneno)
        val phoneText = preferences.getString("phone", null)
        phoneTextView.text = phoneText

        val nameTextView = findViewById<TextView>(R.id.saved_name)
        val aboutTextView = findViewById<TextView>(R.id.saved_about)

        val nameText = preferences.getString("name", null)
        nameTextView.text = nameText

        val aboutText = preferences.getString("about", null)
        aboutTextView.text = aboutText
    }


    //update profile details
    private fun updateProfile() {

        //demo data
        val profileData = ProfilePayload("Zuri chat member",
            "PorayMan",
            "09876543212")

        val call: Call<ProfileResponse> = retrofitService.updateProfile(Constants.ORG_ID, Constants.MEM_ID, profileData)

        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>?
            ) {

                if(response!!.isSuccessful) {
                    Log.i("Login Response Result", response.body()!!.message)

                } else {
                    when(response.code()){
                        400 -> {
                            Log.e("Error 400", "invalid authorization")
                        }
                        404 -> {
                            Log.e("Error 404", "Not Found")
                        }
                        401 -> {
                            Log.e("Error 401", "No authorization or session expired")
                        }
                        else -> {
                            Log.e("Error", "Generic Error")
                        }
                    }
                }

            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Timber.e(t.message.toString())
            }

        })
    }

}



