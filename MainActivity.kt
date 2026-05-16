package com.example.securepostskotlin

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.securepostskotlin.adapter.PostAdapter
import com.example.securepostskotlin.model.Post
import com.example.securepostskotlin.network.RetrofitClient
import okhttp3.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var btnVolley: Button
    private lateinit var btnOkHttp: Button
    private lateinit var btnRetrofit: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var postsList = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVolley = findViewById(R.id.btnLoadVolley)
        btnOkHttp = findViewById(R.id.btnLoadOkHttp)
        btnRetrofit = findViewById(R.id.btnLoadRetrofit)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(postsList)
        recyclerView.adapter = adapter

        btnVolley.setOnClickListener { loadWithVolley() }
        btnOkHttp.setOnClickListener { loadWithOkHttp() }
        btnRetrofit.setOnClickListener { loadWithRetrofit() }
    }

    private fun loadWithVolley() {
        showLoading(true)
        val url = "https://jsonplaceholder.typicode.com/posts"

        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                parseVolleyResponse(response)
                showLoading(false)
            },
            { error ->
                showLoading(false)
                Toast.makeText(this, "Erreur Volley: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun parseVolleyResponse(response: String) {
        try {
            val jsonArray = JSONArray(response)
            postsList.clear()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val post = Post(
                    userId = jsonObject.getInt("userId"),
                    id = jsonObject.getInt("id"),
                    title = jsonObject.getString("title"),
                    body = jsonObject.getString("body")
                )
                postsList.add(post)
            }

            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Volley: ${postsList.size} posts chargés", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Erreur parsing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadWithOkHttp() {
        showLoading(true)

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val request = okhttp3.Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@MainActivity, "Erreur OkHttp: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val data = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && data != null) {
                        parseOkHttpResponse(data)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@MainActivity, "Erreur HTTP: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun parseOkHttpResponse(response: String) {
        try {
            val jsonArray = JSONArray(response)
            postsList.clear()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val post = Post(
                    userId = jsonObject.getInt("userId"),
                    id = jsonObject.getInt("id"),
                    title = jsonObject.getString("title"),
                    body = jsonObject.getString("body")
                )
                postsList.add(post)
            }

            adapter.notifyDataSetChanged()
            showLoading(false)
            Toast.makeText(this, "OkHttp: ${postsList.size} posts chargés", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this, "Erreur parsing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadWithRetrofit() {
        showLoading(true)

        RetrofitClient.apiService.getPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        postsList.clear()
                        postsList.addAll(posts)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(
                            this@MainActivity,
                            "Retrofit: ${posts.size} posts chargés",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Erreur HTTP: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@MainActivity, "Échec Retrofit: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}