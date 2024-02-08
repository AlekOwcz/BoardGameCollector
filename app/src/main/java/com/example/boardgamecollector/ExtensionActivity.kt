package com.example.boardgamecollector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import com.example.boardgamecollector.databinding.ActivityExtensionBinding
import com.example.boardgamecollector.databinding.ActivityGameListBinding
import com.example.databaseexample.MyDBHandler
import java.util.concurrent.Executors

class ExtensionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExtensionBinding
    var gameList: MutableList<Game>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExtensionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = MyDBHandler(this,null,null,1)
        gameList = db.findExt()
        val n = gameList!!.count()
        showData(gameList)
    }

    fun showGames(games:List<Game>) {
        val leftRowMargin = 0
        val topRowMargin = 0
        val rightRowMargin = 0
        val bottomRowMargin = 0
        val bigTextSize = resources.getDimension(R.dimen.font_size_big).toInt()
        val smallTextSize = resources.getDimension(R.dimen.font_size_small).toInt()
        val mediumTextSize = resources.getDimension(R.dimen.font_size_medium).toInt()
        var Nd = 1
        val rows = games.count()
        supportActionBar!!.setTitle("Expansions")
        var textSpacer: TextView? = null


        for (i in -1..rows - 1) {
            var row: Game? = null

            if (i < 0) {
                textSpacer = TextView(this)
                textSpacer.text = ""
            } else {
                row = games.get(i)
            }

            val tvThumbnailHead = TextView(this)
            val tvThumbnail = ImageView(this)
            if (i == -1) {
                tvThumbnailHead.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                tvThumbnailHead.gravity = Gravity.LEFT
                tvThumbnailHead.setPadding(20, 15, 20, 15)
                tvThumbnailHead.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
                tvThumbnailHead.text = "Thumbnail"
                tvThumbnailHead.setBackgroundColor(Color.parseColor("#f7f7f7"))
            } else {
                tvThumbnail.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT)
                tvThumbnailHead.setBackgroundColor(Color.parseColor("#ffffff"))
                val executor = Executors.newSingleThreadExecutor()

                val handler = Handler(Looper.getMainLooper())

                var image: Bitmap? = null

                executor.execute {

                    val imageURL = row!!.pic
                    try {
                        val `in` = java.net.URL(imageURL).openStream()
                        image = BitmapFactory.decodeStream(`in`)
                        handler.post {
                            tvThumbnail.setImageBitmap(image)
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val layDesc = LinearLayout(this)
            layDesc.orientation = LinearLayout.VERTICAL
            layDesc.setPadding(20, 15, 20, 15)
            layDesc.setBackgroundColor(Color.parseColor("#f8f8f8"))
            layDesc.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                5.0f)
            val tvName = TextView(this)
            tvName.gravity = Gravity.TOP
            if (i == -1) {
                tvName.setOnClickListener{
                    rearrange("Title")
                }
                tvName.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT)
                tvName.setPadding(0, 0, 0, 0)
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
                tvName.text = "Title"
                tvName.setBackgroundColor(Color.parseColor("#f0f0f0"))
                layDesc.setBackgroundColor(Color.parseColor("#f0f0f0"))
            } else {
                tvName.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT)

                tvName.setPadding(5, 0, 0, 5)
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
                tvName.setBackgroundColor(Color.parseColor("#f8f8f8"))
                tvName.setTextColor(Color.parseColor("#0600ad"))
                tvName.text = row!!.gameName
            }

            layDesc.addView(tvName)
            if (i > -1) {
                val tvNameOG = TextView(this)
                tvNameOG.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                tvNameOG.gravity = Gravity.RIGHT
                tvNameOG.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize.toFloat())
                tvNameOG.setPadding(5, 1, 0, 5)
                tvNameOG.setTextColor(Color.parseColor("#aaaaaa"))
                tvNameOG.setBackgroundColor(Color.parseColor("#f8f8f8"))
                if(row!!.gameName != row.gameOriginalName) tvNameOG.text = "(" + row!!.gameOriginalName + ")"
                else tvNameOG.text = ""
                layDesc.addView(tvNameOG)
            }

            val tvRelease = TextView(this)
            tvRelease.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            tvRelease.gravity = Gravity.LEFT
            tvRelease.setPadding(20, 15, 20, 15)

            if (i == -1) {
                tvRelease.setOnClickListener{
                    rearrange("Release")
                }
                tvRelease.text = "Release"
                tvRelease.setBackgroundColor(Color.parseColor("#f7f7f7"))
                tvRelease.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
            } else {
                if(row!!.release == 0) tvRelease.text = "N/A"
                else tvRelease.text = row!!.release.toString()
                tvRelease.setBackgroundColor(Color.parseColor("#ffffff"))
                tvRelease.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
            }

            val tvId = TextView(this)
            tvId.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            tvId.gravity = Gravity.LEFT
            tvId.setPadding(20, 15, 20, 15)

            if (i == -1) {
                tvId.setOnClickListener{
                    rearrange("ID")
                }
                tvId.text = "Nd"
                tvId.setBackgroundColor(Color.parseColor("#f0f0f0"))
                tvId.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
            } else {
                tvId.text = Nd.toString()
                Nd += 1
                tvId.setBackgroundColor(Color.parseColor("#f8f8f8"))
                tvId.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize.toFloat())
            }
            val tr = TableRow(this)

            tr.id = i + 1
            val trParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT)
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)
            tr.setPadding(10, 0, 10, 0)
            tr.layoutParams = trParams

            tr.addView(tvId)
            if(i==-1) tr.addView(tvThumbnailHead)
            else tr.addView(tvThumbnail)
            tr.addView(layDesc)
            tr.addView(tvRelease)
            binding.gameTable.addView(tr, trParams)

            if (i > -1) {

                val trSep = TableRow(this)
                val trParamsSep = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT)
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)

                trSep.layoutParams = trParamsSep
                val tvSep = TextView(this)
                val tvSepLay = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                tvSepLay.span = 4
                tvSep.layoutParams = tvSepLay
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"))
                tvSep.height = 1

                trSep.addView(tvSep)
                binding.gameTable.addView(trSep, trParamsSep)
            }


        }

    }

    private fun rearrange(s:String) {
        when (s){
            "Title" -> {
                gameList!!.sortBy { it.gameName }
            }
            "Release" -> {
                gameList!!.sortBy { it.release }
            }
            "ID" -> {
                gameList!!.sortBy { it.id }
            }
        }
        showData(gameList)

    }

    fun showData(gameList:MutableList<Game>?) {
        binding.gameTable.removeAllViews()
        if(gameList!=null)showGames(gameList)
    }


}


