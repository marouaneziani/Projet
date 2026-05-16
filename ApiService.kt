package com.example.securepostskotlin.network

import com.example.securepostskotlin.model.Post
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("posts")
    fun getPosts(): Call<List<Post>>

    @GET("posts/{id}")
    fun getPostById(@retrofit2.http.Path("id") id: Int): Call<Post>
}