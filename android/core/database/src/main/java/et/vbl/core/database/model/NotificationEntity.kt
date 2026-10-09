package et.vbl.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAtIso: String,
    val createdAtTimestamp: Long
)
