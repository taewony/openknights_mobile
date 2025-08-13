package com.openknights.feature.project.data.datasource

import android.content.Context
import com.openknights.data.R
import com.openknights.model.Project
import kotlinx.serialization.json.Json
import java.io.IOException

// TODO: Hilt를 사용하여 Context 주입 받도록 변경
class ProjectLocalDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getFakeProjects(): List<Project> {
        return try {
            val jsonString = context.resources.openRawResource(R.raw.fake_projects)
                .bufferedReader()
                .use { it.readText() }
            json.decodeFromString<List<Project>>(jsonString)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            emptyList()
        }
    }
}
