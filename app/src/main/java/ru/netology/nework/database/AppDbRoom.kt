package ru.netology.nework.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.nework.dao.EventsEntity
import ru.netology.nework.dao.UserEntity
import ru.netology.nework.dao.PostDaoRoom
import ru.netology.nework.dao.PostEntity

@Database(entities = [PostEntity::class, UserEntity::class, EventsEntity::class], version = 1)
abstract class AppDbRoom : RoomDatabase() {
    abstract fun postDaoRoom(): PostDaoRoom

    companion object {
        @Volatile
        private var instance: AppDbRoom? = null

        fun getInstance(context: Context): AppDbRoom {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDbRoom::class.java, "app.db")
                .allowMainThreadQueries()
                .build()
    }
}