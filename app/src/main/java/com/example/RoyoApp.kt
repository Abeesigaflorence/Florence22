package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class RoyoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            val resId = resources.getIdentifier("firestore_database_id", "string", packageName)
            val dbId = if (resId != 0) getString(resId) else null
            val app = FirebaseApp.getInstance()
            val db = if (dbId != null) {
                FirebaseFirestore.getInstance(app, dbId)
            } else {
                FirebaseFirestore.getInstance(app)
            }
            db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        } catch (e: Exception) {
            Log.w("RoyoApp", "Firebase initialization notice: ${e.message}")
        }
    }
}
