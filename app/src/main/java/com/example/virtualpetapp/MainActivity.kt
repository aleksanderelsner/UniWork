package com.example.virtualpetapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.virtualpetapp.model.Pet
import com.example.virtualpetapp.model.SoundPlayer
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset

class MainActivity : AppCompatActivity() {
    lateinit var soundPlayer: SoundPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialise shared preferences
        val sharedPref = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        soundPlayer = SoundPlayer(applicationContext)
        //buttons
        val buttonPlay = findViewById<Button>(R.id.buttonPlay)
        val buttonAdoptionInfo = findViewById<Button>(R.id.buttonAdoptionInformation)
        val buttonSound = findViewById<Button>(R.id.buttonSound)
        val buttonSettings = findViewById<Button>(R.id.buttonSettings)
        val buttonDonate = findViewById<Button>(R.id.buttonDonate)
        //button listeners
        buttonPlay.setOnClickListener {
            play()
            soundPlayer.playButtonSound()
        }
        buttonAdoptionInfo.setOnClickListener {
            adoptionInfo()
            soundPlayer.playButtonSound()
        }
        buttonSettings.setOnClickListener {
            settings()
            soundPlayer.playButtonSound()
        }
        buttonSound.setOnClickListener {
            sound()
            soundPlayer.playButtonSound()
        }
        buttonDonate.setOnClickListener {
            donate()
            soundPlayer.playButtonSound()
        }
        //check if user opened the app first time
        var username = sharedPref.getString("username", "default")
        if (username == "default") {
            login()
            findViewById<TextView>(R.id.textViewGreeting).text = "Welcome $username"
        }
        //if user already inputted his name show it on screen
        else {
            findViewById<TextView>(R.id.textViewGreeting).text = "Welcome $username"
        }
    }

    //button methods
    private fun play() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }


    private fun adoptionInfo() {
        val intent = Intent(this, AdoptionInfoActivity::class.java)
        startActivity(intent)
    }

    private fun settings() {
        val sharedPref = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.settings_layout, null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        //buttons and editTexts from the popup
        val buttonConfirmUsername =
            popupView.findViewById<Button>(R.id.buttonSettingsUsernameConfirm)
        val buttonConfirmPetName = popupView.findViewById<Button>(R.id.buttonSettingsPetNameConfirm)
        val buttonReset = popupView.findViewById<Button>(R.id.buttonSettingsReset)
        val buttonClose = popupView.findViewById<Button>(R.id.buttonSettingsClose)
        val editTextUsername = popupView.findViewById<EditText>(R.id.editTextSettingsUsername)
        val editTextPetName = popupView.findViewById<EditText>(R.id.editTextSettingsPetName)
        //draw the dialog on screen
        dialog.setCancelable(false)
        dialog.show()
        //button listeners
        buttonClose.setOnClickListener {
            soundPlayer.playButtonSound()
            dialog.dismiss()
        }
        buttonReset.setOnClickListener {
            soundPlayer.playButtonSound()
            sharedPref.edit().putString("petname", "default").commit()
            sharedPref.edit().putInt("hiScore", 0).commit()
            sharedPref.edit().putLong(
                GameActivity.SHARED_PREF_LOGOUT_TIME,
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            )
            deleteFile(GameActivity.FILE_PET)
            deleteFile(GameActivity.FILE_INVENTORY)
        }
        buttonConfirmUsername.setOnClickListener {
            soundPlayer.playButtonSound()
            val toast = Toast.makeText(this, "Please input your name first", Toast.LENGTH_SHORT)
            if (editTextUsername.text.toString().equals("")) {
                toast.show()
            } else {
                sharedPref.edit().putString("username", editTextUsername.text.toString()).commit()
                findViewById<TextView>(R.id.textViewGreeting).text =
                    "Welcome ${editTextUsername.text.toString()}"
                toast.setText("Username changed")
                toast.show()
            }
        }
        buttonConfirmPetName.setOnClickListener {
            soundPlayer.playButtonSound()
            val toast = Toast.makeText(this, "Please input pet name first", Toast.LENGTH_SHORT)
            if (editTextPetName.text.toString().equals("")) {
                toast.show()
            } else {
                val fileInput = applicationContext.openFileInput("progress")
                val objectInputStream = ObjectInputStream(fileInput)
                val pet = objectInputStream.readObject() as Pet
                fileInput.close()
                objectInputStream.close()
                pet.name =
                    popupView.findViewById<EditText>(R.id.editTextSettingsPetName).text.toString()
                val fileOutput = applicationContext.openFileOutput("progress", Context.MODE_PRIVATE)
                val objectOutputStream = ObjectOutputStream(fileOutput)
                objectOutputStream.writeObject(pet)
                objectOutputStream.close()
                fileOutput.close()
                toast.setText("PetName Changed")
                toast.show()
            }
        }
    }

    private fun sound() {
        val buttonSound = findViewById<Button>(R.id.buttonSound)
        val sharedPref = getSharedPreferences(GameActivity.SHARED_PREF,Context.MODE_PRIVATE)
        val soundOn = getDrawable(R.drawable.ic_baseline_volume_up_24)
        val soundOff = getDrawable(R.drawable.ic_baseline_volume_off_24)
        if(sharedPref.getBoolean("mute",false)){
            sharedPref.edit().putBoolean("mute",false).commit()
            buttonSound.background = soundOn
        }else{
            sharedPref.edit().putBoolean("mute",true).commit()
            buttonSound.background = soundOff
        }
    }

    private fun donate() {
        var url = "https://www.leicesteranimalaid.org.uk/donate/donate"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    //first app open method
    private fun login() {
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.login_layout, null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        val buttonContinue = popupView.findViewById<Button>(R.id.buttonLoginContinue)
        val editTextName = popupView.findViewById<EditText>(R.id.editTextLoginUsername)
        dialog.setCancelable(false)
        dialog.show()
        buttonContinue.setOnClickListener {
            soundPlayer.playButtonSound()
            if (editTextName.text.toString().equals("")) {
                val toast = Toast.makeText(this, "Please input your name first", Toast.LENGTH_SHORT)
                toast.show()
            } else {
                val sharedPref = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
                sharedPref.edit().putString("username", editTextName.text.toString()).apply()
                dialog.dismiss()
            }
        }
    }
}