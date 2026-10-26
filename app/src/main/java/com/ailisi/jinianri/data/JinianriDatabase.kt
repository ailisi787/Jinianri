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
    version = 4,
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

        /** v3 -> v4：事件新增节日规则字段（空 = 普通事件，见 FestivalRules） */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN rule TEXT NOT NULL DEFAULT ''")
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .addCallback(object : RoomDatabase.Callback() {
                    /**
                     * 每次打开数据库前先把 WAL 合并回主库并清空 WAL。
                     *
                     * 覆盖安装 / 被系统强杀时，最后写入的数据可能还留在 `-wal` 里没落进主库。
                     * 一旦 WAL 被判废（校验和或 salt 不匹配），SQLite 会直接忽略它，
                     * 界面就会表现为「事件全部消失了」——数据其实还在 WAL 文件中。
                     * 这里在 onOpen 里做 TRUNCATE checkpoint，早于任何查询，
                     * 让主库始终自包含，下次异常结束也不会再出现这种假空。
                     *
                     * 其它连接正占用时 SQLite 会返回 busy，忽略即可（下次启动再合并）。
                     */
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        try {
                            db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
                        } catch (_: Exception) {
                            // busy / 只读文件系统等情况：放弃本次合并，不影响正常使用
                        }
                    }

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
