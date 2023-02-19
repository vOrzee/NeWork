package ru.netology.nework.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nework.dao.*

@Database(entities = [PostEntity::class, UserEntity::class, EventsEntity::class, JobEntity::class], version = 1)
abstract class AppDbRoom : RoomDatabase() {
    abstract fun postDaoRoom(): PostDaoRoom
}