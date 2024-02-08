package com.example.boardgamecollector

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamecollector.databinding.ActivitySynchronizationBinding
import com.example.databaseexample.MyDBHandler
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


class SynchronizationActivity : AppCompatActivity() {

    private var userName = ""
    private lateinit var binding: ActivitySynchronizationBinding
    var ownedGames = 0
    var ownedExt = 0
    var lastSync = "Never"
    private var boardgameList:MutableList<Game> = mutableListOf()
    private var expansionList:MutableList<Game> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySynchronizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extra = intent.extras?.getBoolean("first")
        if(extra!!) {
            displayConfig(false)
        } else {
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            userName = prefs.getString("name","")!!
            lastSync = prefs.getString("lastSync","")!!
            binding.tvSync.text = "Last Synchronization: $lastSync"
        }
        binding.btnReturn.setOnClickListener{
            finish()
        }
        binding.btnSync.setOnClickListener{
            val curDate = (LocalDate.now().toString() + " " + LocalTime.now().toString()).substring(0..18)
            var newDay = ((lastSync.substring(8..9)).toInt()+1).toString()
            if (newDay.length ==1) newDay = "0$newDay"
            val check = lastSync.substring(0..7) + newDay + lastSync.substring(10..18)

            val dateCur = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(curDate)
            val dateCheck = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(check)

            if(dateCur.before(dateCheck)) confirmSync(curDate)
            else sync(curDate)
        }

    }

    private fun confirmSync(curDate: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure?")
        builder.setMessage("Game rankings are updated only once a day, synchronize only if you updated your list")
        builder.setPositiveButton(
            "Confirm"
        ) { dialog, _ ->
            dialog.dismiss()
            sync(curDate)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

    }

    private fun displayConfig(failed:Boolean){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Configuration")
        if(failed) builder.setMessage("Error, couldn't find the user, please try again")
        else builder.setMessage("Please enter your GBG username")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton(
            "Enter"
        ) { dialog, _ ->
            dialog.dismiss()
            userName = input.text.toString()
            if(userName=="") displayConfig(false)
            else initialConfig()
        }
        builder.show()
        return
    }

    fun syncData(curDate: String){

        val db = MyDBHandler(this,null,null,1)
        ownedGames = db.countGames()
        ownedExt = db.countExtensions()
        lastSync = curDate
        binding.tvSync.text = "Last Synchronization: $curDate"
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("name",userName)
        editor.putInt("ownedGames",ownedGames)
        editor.putInt("ownedExt",ownedExt)
        editor.putString("lastSync", lastSync)
        editor.commit()
        finish()
    }

    private fun sync(curDate: String){
        Toast.makeText(this, "Synchronizing...", Toast.LENGTH_SHORT).show()
        binding.tvSync.text = "Last Synchronization: Performing synchronization..."
        binding.tvSync.text = "Last Synchronization: Fetching user's list..."
        downloadXMLG()
        loadData("ulg.xml", 0)
        binding.tvSync.text = "Last Synchronization: Comparing lists..."
        val db = MyDBHandler(this,null,null,1)
        var oldgameList = db.findGames()
        var toRemove = db.findGames()
        for (i in 0..boardgameList.count()-1){
            addDate(boardgameList[i], curDate.substring(0..9))
        }

        oldgameList.sortBy { it.id }
        toRemove.sortBy { it.id }
        boardgameList.sortBy { it.id }
        var remov = 0
        for (i in 0..oldgameList.count()-1){
            val gid = oldgameList[i].id
            for(j in 0..boardgameList.count()-1){
                if(boardgameList[j].id == gid) {
                    toRemove.removeAt(i-remov)
                    remov += 1
                    break
                }
            }
        }

        for (i in 0..oldgameList.count()-1){
            val gid = oldgameList[i].id
            for(j in 0..boardgameList.count()-1){
                if(boardgameList[j].id == gid) {
                    boardgameList.removeAt(j)
                    break
                }
            }
        }
        for (i in 0..boardgameList.count()-1){
            addGame(boardgameList[i])
        }
        binding.tvSync.text = "Last Synchronization: Fetching user's expansion list..."
        downloadXMLE()

        loadData("ule.xml",1)

        binding.tvSync.text = "Last Synchronization: Loading expansions into database..."

        var oldgameListE = db.findExt()
        var toRemoveE = db.findExt()
        for (i in 0..expansionList.count()-1){
            addDate(expansionList[i], curDate.substring(0..9))
        }
        oldgameListE.sortBy { it.id }
        toRemoveE.sortBy { it.id }
        expansionList.sortBy { it.id }

        var removE = 0
        for (i in 0..oldgameListE.count()-1){
            val gid = oldgameListE[i].id
            for(j in 0..expansionList.count()-1){
                if(expansionList[j].id == gid) {
                    toRemoveE.removeAt(i - removE)
                    removE += 1
                    break
                }
            }
        }
        for (i in 0..oldgameListE.count()-1){
            val gid = oldgameListE[i].id
            for(j in 0..expansionList.count()-1){
                if(expansionList[j].id == gid) {
                    expansionList.removeAt(j)
                    break
                }
            }
        }

        for (i in 0..expansionList.count()-1){

            addGame(expansionList[i])
        }

        db.close()
        if(toRemove.count()>0 || toRemoveE.count()>0) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("List mismatch detected")
            builder.setMessage("Some entries stored in the database are missing from your list. Do you want to delete these entries?")
            builder.setPositiveButton(
                "Confirm"
            ) { dialog, _ ->
                dialog.dismiss()
                delExtra(toRemove,toRemoveE)
                syncData(curDate)
            }
            builder.setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                dialog.dismiss()
                syncData(curDate)
            }
            builder.show()
        }
        else syncData(curDate)
    }

    fun delExtra(toRemoveG:MutableList<Game>,toRemoveE:MutableList<Game>){
        val db = MyDBHandler(this,null,null,1)
        if(toRemoveG.count()>0) {
            for (i in 0..toRemoveG.count() - 1) {
                db.deleteGame(toRemoveG[i].id)
            }
        }
        if(toRemoveE.count()>0) {
            for (i in 0..toRemoveE.count() - 1) {
                db.deleteGame(toRemoveE[i].id)
            }
        }
        db.close()
    }


    private fun initialConfig(){
        Toast.makeText(this, "Synchronizing...", Toast.LENGTH_SHORT).show()
        val curDate = (LocalDate.now().toString() + " " + LocalTime.now().toString()).substring(0..18)
        binding.tvSync.text = "Last Synchronization: Performing synchronization..."
        binding.tvSync.text = "Last Synchronization: Fetching user's list..."
        downloadXMLG()

        val failed = loadData("ulg.xml", 0)
        if(failed) {
            displayConfig(true)
            return
        }

        binding.tvSync.text = "Last Synchronization: Loading games into database..."
        for (i in 0..boardgameList.count()-1){
            addGame(boardgameList[i])

            addDate(boardgameList[i], curDate.substring(0..9))

        }
        binding.tvSync.text = "Last Synchronization: Fetching user's expansion list..."
        downloadXMLE()

        loadData("ule.xml",1)
        binding.tvSync.text = "Last Synchronization: Loading expansions into database..."
        for (i in 0..expansionList.count()-1){
            addGame(expansionList[i])
        }

        binding.tvSync.text = "Last Synchronization: Loading games and expansions..."

        val db = MyDBHandler(this,null,null,1)
        ownedGames = db.countGames()
        ownedExt = db.countExtensions()
        lastSync = curDate
        binding.tvSync.text = "Last Synchronization: $curDate"
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstBoot", false)
        editor.putString("name",userName)
        editor.putInt("ownedGames",ownedGames)
        editor.putInt("ownedExt",ownedExt)
        editor.putString("lastSync", lastSync)
        editor.commit()
        db.close()
        finish()
    }

    private fun addGame(game: Game) {
        val db = MyDBHandler(this,null,null,1)
        if(!db.findGame(game.id)) db.addGame(game)
        db.close()
    }

    private fun addDate(game: Game, curDate: String){
        val db = MyDBHandler(this,null,null,1)
        if(!db.findDate(game.id,curDate)) db.addDate(game, curDate)
        db.close()
    }


    fun loadData(filename: String, type: Int): Boolean {
        val path = filesDir
        val inDir = File(path, "XMLs")
        if (inDir.exists()) {
            val file = File(inDir, filename)
            if (file.exists()) {
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

                xmlDoc.documentElement.normalize()
                val messageWait = xmlDoc.getElementsByTagName("message")
                if(messageWait.length > 0) {
                    val msg = messageWait.item(0)
                    if(msg.textContent.equals("Invalid username specified")) return true
                    else {
                        binding.tvSync.text = "Last Synchronization: Waiting for response from boardgamegeek.com..."
                        sleep(5000)
                        if(type==0) downloadXMLG()
                        else downloadXMLE()
                        val failed = loadData(filename, type)
                        if(failed) return true
                    }
                }
                val items: NodeList = xmlDoc.getElementsByTagName("item")
                if (items.length == 0) return true
                for (i in 0..items.length-1) {
                    val itemNode: Node = items.item(i)
                    if(itemNode.nodeType == Node.ELEMENT_NODE) {
                        val elem = itemNode as Element
                        val children = elem.childNodes
                        val currentId: Long = elem.getAttribute("objectid").toLong()
                        var currentName:String? = null
                        var currentOGName:String? = null
                        var currentYearPub:Int = 0
                        var currentImage:String? = null
                        var currentRank:Int? = null
                        for(j in 0..children.length-1) {
                            val node = children.item(j)
                            if (node is Element) {
                                when (node.nodeName) {
                                    "name" -> {
                                        currentName = node.textContent
                                        currentOGName = node.textContent
                                    }
                                    "originalname" -> {
                                        currentOGName = node.textContent
                                    }
                                    "yearpublished" -> {
                                        currentYearPub = node.textContent.toInt()
                                    }
                                    "thumbnail" -> {
                                        currentImage = node.textContent
                                    }
                                    "stats" -> {
                                        val x = node.getElementsByTagName("rank")
                                        val y = x.item(0) as Element
                                        val z = y.getAttribute("value")
                                        if (z == "Not Ranked") currentRank = 0
                                        else currentRank = z.toInt()
                                    }
                                }
                            }
                        }
                        if (currentName != null && currentRank != null) {
                            if(currentImage == null) {
                                val g=Game(currentId,currentName,currentOGName!!,currentYearPub,currentRank,type)
                                if(type==0) boardgameList.add(g)
                                else expansionList.add(g)
                            } else {
                                val g=Game(currentId,currentName,currentOGName!!,currentYearPub,currentRank,currentImage,type)
                                if(type==0) boardgameList.add(g)
                                else expansionList.add(g)
                            }
                        }
                    }
                }
            }
        }
        var filetodel = File(inDir, "ulg.xml")
        filetodel.delete()
        filetodel = File(inDir, "ule.xml")
        filetodel.delete()
        return false
    }


    fun downloadXMLG(){
        val url = URL("https://www.boardgamegeek.com/xmlapi2/collection?username="+userName+"&stats=1&excludesubtype=boardgameexpansion")
        val connection = url.openConnection()
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        connection.connect()
        val isStream = url.openStream()
        val testDirectory = File("$filesDir/XMLs")
        if (!testDirectory.exists()) testDirectory.mkdir()
        val fos = FileOutputStream("$testDirectory/ulg.xml")
        val data = ByteArray(1024)
        var count = 0
        var total:Long = 0
        count = isStream.read(data)
        while(count != -1) {
            total += count.toLong()
            fos.write(data, 0, count)
            count = isStream.read(data)
        }
        isStream.close()
        fos.close()
    }


    fun downloadXMLE(){
        val url = URL("https://www.boardgamegeek.com/xmlapi2/collection?username=$userName&stats=1&subtype=boardgameexpansion")
        val connection = url.openConnection()
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        connection.connect()
        val lengthOfFile = connection.contentLength
        val isStream = url.openStream()
        val testDirectory = File("$filesDir/XMLs")
        if (!testDirectory.exists()) testDirectory.mkdir()
        val fos = FileOutputStream("$testDirectory/ule.xml")
        val data = ByteArray(1024)
        var count = 0
        var total:Long = 0
        var progress = 0
        count = isStream.read(data)
        while(count != -1) {
            total += count.toLong()
            val progress_temp = total.toInt()*100/lengthOfFile
            if(progress_temp % 10 == 0 && progress != progress_temp) {
                progress = progress_temp
            }
            fos.write(data, 0, count)
            count = isStream.read(data)
        }
        isStream.close()
        fos.close()
    }

}