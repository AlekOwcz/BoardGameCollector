package com.example.databaseexample

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.boardgamecollector.Game
import com.example.boardgamecollector.Rank
import java.text.SimpleDateFormat
import java.util.*

class MyDBHandler(context: Context, name: String?,
                  factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context,
    DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "gameboardDB.db"
        val TABLE_GAMES = "games"
        val COLUMN_TITLE = "title"
        val COLUMN_TITLE_OG = "title_original"
        val COLUMN_RELEASE = "release"
        val COLUMN_ID = "game_id"
        val COLUMN_RANKING = "ranking"
        val COLUMN_PIC = "picture"
        val COLUMN_TYPE = "type"

        val TABLE_RANKINGS = "ranking_history"
        val COLUMN_DATE = "date"
        val COLUMN_RANK = "rank"
    }

    fun getDBName(): String{
        return DATABASE_NAME
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAME_TABLE = ("CREATE TABLE " + TABLE_GAMES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, "+
                COLUMN_TITLE +" TEXT, " +
                COLUMN_TITLE_OG +" TEXT, " +
                COLUMN_RELEASE + " INTEGER, " +
                COLUMN_RANKING + " INTEGER, " +
                COLUMN_PIC + " TEXT NULL, " +
                COLUMN_TYPE + " INTEGER " +
                ")")
        db.execSQL(CREATE_GAME_TABLE)

        val CREATE_RANK_TABLE = ("CREATE TABLE " + TABLE_RANKINGS + "(" +
                COLUMN_ID + " INTEGER NOT NULL, "+
                COLUMN_DATE + " TEXT, " +
                COLUMN_RANK + " INTEGER, " +
                "PRIMARY KEY ($COLUMN_ID,$COLUMN_DATE)" +
                ")")
        db.execSQL(CREATE_RANK_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RANKINGS)
        onCreate(db)
    }

    fun resetData(){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_GAMES)
        db.execSQL("DELETE FROM " + TABLE_RANKINGS)
        db.close()
    }

    fun countGames(): Int {
        val db = this.writableDatabase
        val query = "SELECT COUNT($COLUMN_ID) FROM $TABLE_GAMES WHERE $COLUMN_TYPE = 0"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val ret = cursor.getInt(0)
        cursor.close()
        return ret
    }

    fun countExtensions(): Int {
        val db = this.writableDatabase
        val query = "SELECT COUNT($COLUMN_ID) FROM $TABLE_GAMES WHERE $COLUMN_TYPE = 1"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val ret = cursor.getInt(0)
        cursor.close()
        return ret
    }

    fun addGame(game: Game) {
        val values = ContentValues()
        values.put(COLUMN_ID, game.id)
        values.put(COLUMN_TITLE, game.gameName)
        values.put(COLUMN_TITLE_OG, game.gameOriginalName)
        values.put(COLUMN_RELEASE, game.release)
        values.put(COLUMN_RANKING, game.ranking)
        values.put(COLUMN_PIC, game.pic.toString())
        values.put(COLUMN_TYPE, game.type)

        val db = this.writableDatabase
        db.insert(TABLE_GAMES, null, values)
        db.close()
    }

    fun findGames(): MutableList<Game> {
        val query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_TYPE = 0"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var game: Game? = null
        var games:MutableList<Game> = mutableListOf()
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val gameid = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val gametitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val gametitleog = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE_OG))
            val gamerelease = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RELEASE))
            val gamerank = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANKING))
            val gamepic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIC))
            val gametype = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
            if(gamepic==null) game = Game(gameid,gametitle,gametitleog,gamerelease,gamerank,gametype)
            else game = Game(gameid,gametitle,gametitleog,gamerelease,gamerank,gamepic,gametype)
            games.add(game)
            cursor.moveToNext()
        }
        cursor.close()
        return games
    }

    fun findGame(id:Long):Boolean{
        val query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ID = $id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        return !cursor.isAfterLast
    }

    fun findRanks(id: Long): MutableList<Rank> {
        val query = "SELECT * FROM $TABLE_RANKINGS WHERE $COLUMN_ID = $id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var r: Rank? = null
        var rankList:MutableList<Rank> = mutableListOf()
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val gd = cursor.getString(1)
            val gr = cursor.getInt(2)
            r = Rank(gr,gd)
            rankList.add(r)
            cursor.moveToNext()
        }
        cursor.close()
        return rankList
    }

    fun findExt(): MutableList<Game> {
        val query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_TYPE = 1"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var ext: Game? = null
        var extList:MutableList<Game> = mutableListOf()
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val gameid = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val gametitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val gametitleog = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE_OG))
            val gamerelease = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RELEASE))
            val gamerank = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANKING))
            val gamepic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIC))
            val gametype = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
            if(gamepic==null) ext = Game(gameid,gametitle,gametitleog,gamerelease,gamerank,gametype)
            else ext = Game(gameid,gametitle,gametitleog,gamerelease,gamerank,gamepic,gametype)
            extList.add(ext)
            cursor.moveToNext()
        }
        return extList
    }
    fun findDate(id:Long, date: String):Boolean{
        val query = "SELECT * FROM $TABLE_RANKINGS WHERE $COLUMN_ID = $id AND $COLUMN_DATE = $date"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        return !cursor.isAfterLast
    }
    fun addDate(game: Game, date: String) {
        val values = ContentValues()
        values.put(COLUMN_ID, game.id)
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_RANK, game.ranking)
        val db = this.writableDatabase
        db.insert(TABLE_RANKINGS, null, values)
        db.close()
    }

    fun deleteGame(gameid: Long) {
        val query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ID = $gameid"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if(cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            db.delete(TABLE_GAMES, "$COLUMN_ID = ?", arrayOf(id.toString()))
            cursor.close()
        }

        val query2 = "SELECT * FROM $TABLE_RANKINGS WHERE $COLUMN_ID = $gameid"
        val cursor2 = db.rawQuery(query2,null)
        cursor2.moveToFirst()
        while(!cursor2.isAfterLast){
            val id = cursor2.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            db.delete(TABLE_RANKINGS, "$COLUMN_ID = ?", arrayOf(id.toString()))
            cursor2.moveToNext()
        }
        cursor2.close()
        db.close()
    }
}