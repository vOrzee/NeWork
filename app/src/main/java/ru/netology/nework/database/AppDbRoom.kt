package ru.netology.nework.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nework.dao.EventsEntity
import ru.netology.nework.dao.UserEntity
import ru.netology.nework.dao.PostDaoRoom
import ru.netology.nework.dao.PostEntity

@Database(entities = [PostEntity::class, UserEntity::class, EventsEntity::class], version = 1)
abstract class AppDbRoom : RoomDatabase() {
    abstract fun postDaoRoom(): PostDaoRoom
}