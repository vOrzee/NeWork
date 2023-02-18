package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.*

interface ApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: PostCreateRequest): Response<PostResponse>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<MediaResponse>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(@Field("login") login: String, @Field("password") pass: String): Response<Token>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun register(
        @Field("login") login: String,
        @Field("password") pass: String,
        @Field("name") name: String
    ): Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithPhoto(
        @Part("login") login: RequestBody,
        @Part("password") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<Token>

    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    @GET("events")
    suspend fun getAllEvents(): Response<List<EventResponse>>

    @POST("events")
    suspend fun saveEvents(@Body event: EventCreateRequest): Response<EventResponse>

    @DELETE("events/{id}")
    suspend fun removeByIdEvent(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeByIdEvent(@Path("id") id: Long): Response<EventResponse>

    @DELETE("events/{id}/likes")
    suspend fun dislikeByIdEvent(@Path("id") id: Long): Response<EventResponse>

    @POST("events/{id}/participants")
    suspend fun joinByIdEvent(@Path("id") id: Long): Response<EventResponse>

    @DELETE("events/{id}/participants")
    suspend fun unJoinByIdEvent(@Path("id") id: Long): Response<EventResponse>
}