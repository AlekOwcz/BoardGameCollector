package com.example.boardgamecollector

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.boardgamecollector.databinding.ActivityMainBinding
import com.example.databaseexample.MyDBHandler
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {

    private var userName = ""
    private lateinit var binding: ActivityMainBinding
    var welcomeMessage = "Welcome"
    var ownedGames = 0
    var ownedExt = 0
    var lastSync = "Never"

    private var boardgameList:MutableList<Game> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val firstBoot = prefs.getBoolean("firstBoot", true)
        if(firstBoot) {
            val i = Intent(this, SynchronizationActivity::class.java)
            i.putExtra("first",true)
            startActivityForResult(i,1000)
        } else {
            userName = prefs.getString("name","").toString()
            lastSync = prefs.getString("lastSync","").toString()
            ownedGames = prefs.getInt("ownedGames", 0)
            ownedExt = prefs.getInt("ownedExt",0)
            welcomeMessage = "Welcome $userName"
            binding.tvWelcome.text = welcomeMessage
            binding.btnGames.text = "Games Owned: $ownedGames"
            binding.btnExtensions.text = "Expansions Owned: $ownedExt"
            binding.btnSynchronization.text = "Last Synchronization: $lastSync"
        }

        binding.btnGames.setOnClickListener{
            val i = Intent(this, GameListActivity::class.java)
            startActivity(i)
        }

        binding.btnExtensions.setOnClickListener{
            val i = Intent(this, ExtensionActivity::class.java)
            startActivity(i)
        }

        binding.btnSynchronization.setOnClickListener{
            synchronize()
        }

        binding.btnClear.setOnClickListener{
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Clear all data")
            builder.setMessage("Are you sure? This action is irreversible and will restart the app")
            builder.setPositiveButton(
                "Confirm"
            ) { dialog, _ ->
                dialog.dismiss()
                clearAllData()
            }

            builder.setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        userName = prefs.getString("name","").toString()
        lastSync = prefs.getString("lastSync","").toString()
        ownedGames = prefs.getInt("ownedGames", 0)
        ownedExt = prefs.getInt("ownedExt",0)
        welcomeMessage = "Welcome $userName"
        binding.tvWelcome.text = welcomeMessage
        binding.btnGames.text = "Games Owned: $ownedGames"
        binding.btnExtensions.text = "Expansions Owned: $ownedExt"
        binding.btnSynchronization.text = "Last Synchronization: $lastSync"

    }
    private fun clearAllData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
        val db = MyDBHandler(this,null,null,1)
        db.resetData()
        val dbName = db.getDBName()
        db.close()
        this.deleteDatabase(dbName)
        val inDir = File(filesDir, "XMLs")
        var file = File(inDir, "ulg.xml")
        file.delete()
        file = File(inDir, "ule.xml")
        file.delete()
        Toast.makeText(this, "Clearing data...", Toast.LENGTH_SHORT).show()
        this.finish()
    }

    fun synchronize(){
        val i = Intent(this, SynchronizationActivity::class.java)
        i.putExtra("first",false)
        startActivityForResult(i,1000)
    }

}