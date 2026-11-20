package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    @Volatile
    private var instance: FirebaseFirestore? = null

    fun get(context: Context): FirebaseFirestore {
        return instance ?: synchronized(this) {
            instance ?: run {
                val dbId = try {
                    context.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    "ai-studio-android-royofarm-9a7cecef-dd3b-458f-8236-ea77210bd822"
                }
                val app = if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
                } else {
                    FirebaseApp.getInstance()
                }
                val db = FirebaseFirestore.getInstance(app, dbId)
                instance = db
                db
            }
        }
    }

    // Direct injection for tests
    fun setTestInstance(db: FirebaseFirestore) {
        instance = db
    }
}
