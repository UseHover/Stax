package com.hover.stax.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

internal class StaxFirebase {
	private val settings = firestoreSettings { isPersistenceEnabled = true }
	private val db = Firebase.firestore.also { it.firestoreSettings = settings }

	fun fetchBonuses() : Task<QuerySnapshot> {
		return db.collection("bonuses").get()
	}
}