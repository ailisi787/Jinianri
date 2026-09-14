package com.ailisi.jinianri.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [EventEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class JinianriDatabase : RoomDatabase() {
    abstract fun events(): EventDao
    abstract fun categories(): CategoryDao

    companion object {
        /** 未分类的系统分类 id 固定为 1 */
        const val UNCATEGORIZED_ID = 1L

        /** v1 -> v2：事件新增图标字段 */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN iconKey TEXT NOT NULL DEFAULT ''")
            }
        }

        /** v2 -> v3：事件新增自定义排序字段（null = 自动按日期） */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN sortOrder INTEGER")
            }
        }

        @Volatile
        private var instance: JinianriDatabase? = null

        fun get(context: Context): JinianriDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): JinianriDatabase =
            Room.databaseBuilder(context, JinianriDatabase::class.java, "jinianri.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 预置分类：未分类（系统）、生日、纪念日、考试
                        db.execSQL(
                            "INSERT INTO categories (id, name, color, icon, sortOrder, isSystem) VALUES " +
                                "(${UNCATEGORIZED_ID}, '未分类', ${0xFF79747E}, 'category', 0, 1)," +
                                "(2, '生日', ${0xFFE91E63}, 'cake', 1, 0)," +
                                "(3, '纪念日', ${0xFF6750A4}, 'favorite', 2, 0)," +
                                "(4, '考试', ${0xFFFF9800}, 'school', 3, 0)"
                        )
                    }
                })
                .build()
    }
}
