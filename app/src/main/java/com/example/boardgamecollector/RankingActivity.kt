package com.example.boardgamecollector

import android.app.ActionBar
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamecollector.databinding.ActivityRankingBinding
import com.example.databaseexample.MyDBHandler

class RankingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRankingBinding
    var rankList:MutableList<Rank>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val extra = intent.extras!!.getString("gameID")
        val db = MyDBHandler(this,null,null,1)
        rankList = db.findRanks(extra!!.toLong())
        showData(rankList)

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    fun showRanks(ranks:List<Rank>) {
        val leftRowMargin = 0
        val topRowMargin = 0
        val rightRowMargin = 0
        val bottomRowMargin = 0
        val bigTextSize = resources.getDimension(R.dimen.font_size_big).toInt()
        val smallTextSize = resources.getDimension(R.dimen.font_size_small).toInt()
        val mediumTextSize = resources.getDimension(R.dimen.font_size_medium).toInt()

        val rows = ranks.count()
        supportActionBar!!.setTitle("Expansions")
        var textSpacer: TextView? = null


        for (i in -1..rows - 1) {
            var row: Rank? = null

            if (i < 0) {
                textSpacer = TextView(this)
                textSpacer.text = ""
            } else {
                row = ranks.get(i)
            }
            val tvRank = TextView(this)
            tvRank.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            tvRank.gravity = Gravity.LEFT
            tvRank.setPadding(20, 15, 20, 15)

            if (i == -1) {
                tvRank.setOnClickListener{
                    rearrange("Rank")
                }
                tvRank.text = "Rank"
                tvRank.setBackgroundColor(Color.parseColor("#f0f0f0"))
                tvRank.setTextSize(TypedValue.COMPLEX_UNIT_PX, bigTextSize.toFloat())
            } else {
                tvRank.text = row!!.rank.toString()
                tvRank.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT)
                tvRank.setBackgroundColor(Color.parseColor("#f8f8f8"))
                tvRank.setTextSize(TypedValue.COMPLEX_UNIT_PX, bigTextSize.toFloat())
            }

            val tvDate = TextView(this)
            tvDate.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            tvDate.gravity = Gravity.LEFT
            tvDate.setPadding(20, 15, 20, 15)
            if (i == -1) {
                tvDate.setOnClickListener{
                    rearrange("Date")
                }
                tvDate.text = "Date"
                tvDate.setBackgroundColor(Color.parseColor("#f7f7f7"))
                tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, bigTextSize.toFloat())
            } else {
                tvDate.text = row!!.date
                tvDate.setBackgroundColor(Color.parseColor("#ffffff"))
                tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, bigTextSize.toFloat())
            }

            val tr = TableRow(this)

            tr.id = i + 1
            val trParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.MATCH_PARENT)
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)
            tr.setPadding(10, 0, 10, 0)
            tr.layoutParams = trParams

            tr.addView(tvRank)
            tr.addView(tvDate)
            binding.rankTable.addView(tr, trParams)

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
                tvSepLay.span = 3
                tvSep.layoutParams = tvSepLay
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"))
                tvSep.height = 3

                trSep.addView(tvSep)
                binding.rankTable.addView(trSep, trParamsSep)
            }


        }

    }
    fun showData(rankList:MutableList<Rank>?) {
        binding.rankTable.removeAllViews()
        if(rankList!=null) showRanks(rankList)
    }
    private fun rearrange(s:String) {
        when (s){
            "Rank" -> {
                rankList!!.sortBy { it.rank }
            }
            "Date" -> {
                rankList!!.sortByDescending { it.date }
            }
        }
        showData(rankList)

    }

}