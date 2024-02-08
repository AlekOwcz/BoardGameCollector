package com.example.boardgamecollector

import java.net.URL

class Game {
    var id: Long = 0
    var gameName: String? = null
    var gameOriginalName: String? = null
    var release: Int = 0
    var ranking: Int = 0
    var pic: String? = "https://cf.geekdo-images.com/zxVVmggfpHJpmnJY9j-k1w__itemrep/img/Py7CTY0tSBSwKQ0sgVjRFfsVUZU=/fit-in/246x300/filters:strip_icc()/pic1657689.jpg"
    var type: Int = 0

    constructor(id:Long, gameName:String, gameOriginalName: String?, release: Int, ranking: Int, pic: String?, type: Int) {
        this.id = id
        this.gameName = gameName
        this.gameOriginalName = gameOriginalName
        this.release = release
        this.ranking = ranking
        this.pic = pic
        this.type = type
    }
    constructor(id:Long, gameName:String, gameOriginalName: String?, release: Int, ranking: Int, type: Int) {
        this.id = id
        this.gameName = gameName
        this.gameOriginalName = gameOriginalName
        this.release = release
        this.ranking = ranking
        this.type = type
    }
}