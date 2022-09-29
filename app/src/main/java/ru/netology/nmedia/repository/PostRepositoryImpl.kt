package ru.netology.nmedia.repository

import android.widget.Toast
import androidx.lifecycle.*
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {

        val postEntityBuf = dao.getPostById(id)

        try {


            dao.removeById(id)

            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: RuntimeException) {
            dao.insert(postEntityBuf)

        }     catch (e: Throwable) {
            //val a = 111
            dao.insert(postEntityBuf)
            //Toast.makeText(this, "afasdf", Toast.LENGTH_LONG).show()
        }
    }

    override suspend fun likeById(id: Long, setLikes: Int, setLikedByMe: Int) {

        val postEntityBuf = dao.getPostById(id)

        try {
            //val postFromServer = PostsApi.service.getById(id).body()

            dao.likeById(id, setLikes, setLikedByMe)

            if (setLikedByMe == 1) {
                val response = PostsApi.service.likeById(id)

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

            } else {
                val response2 = PostsApi.service.dislikeById(id)

                if (!response2.isSuccessful) {
                    throw ApiError(response2.code(), response2.message())
                }
            }


        } catch (e: Throwable) {
            dao.insert(postEntityBuf)
        }
    }
}
