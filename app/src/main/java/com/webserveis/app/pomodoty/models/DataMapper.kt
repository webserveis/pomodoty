package com.webserveis.app.pomodoty.models

import com.webserveis.app.pomodoty.database.ItemDB

object DataMapper {

    fun ItemModel.toItemDB(): ItemDB {
        return ItemDB(
            uid,
            displayName
        )
    }





}
