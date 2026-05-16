package com.example.securepostskotlin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val txtTitle = findViewById<TextView>(R.id.txtDetailTitle)
        val txtBody = findViewById<TextView>(R.id.txtDetailBody)

        val title = intent.getStringExtra("post_title") ?: "Titre non disponible"
        val body = intent.getStringExtra("post_body") ?: "Contenu non disponible"

        txtTitle.text = title
        txtBody.text = body

        supportActionBar?.title = "Détail du post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}