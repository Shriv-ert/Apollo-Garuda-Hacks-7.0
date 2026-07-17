package com.garuda.floatingbubble.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // ─── Report ───────────────────────────────────────────────────────────────

    @POST("report")
    suspend fun submitReport(
        @Header("Authorization") token: String,
        @Body request: SubmitReportRequest
    ): Response<ApiResponse<ReportItem>>

    @GET("reports/history")
    suspend fun getReportHistory(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedApiResponse<ReportItem>>

    @GET("reports/{id}")
    suspend fun getReportDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<ReportItem>>

    // ─── Check / Scan ─────────────────────────────────────────────────────────

    @POST("check")
    suspend fun checkText(
        @Header("Authorization") token: String,
        @Body request: CheckRequest
    ): Response<ApiResponse<CheckResult>>

    @Multipart
    @POST("check/image")
    suspend fun checkImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<CheckImageResponse>>

    // ─── Entity (Database) ────────────────────────────────────────────────────

    @GET("entities")
    suspend fun getEntities(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<PaginatedApiResponse<EntityItem>>

    // ─── Graph ────────────────────────────────────────────────────────────────

    @GET("graph")
    suspend fun getGraph(
        @Header("Authorization") token: String
    ): Response<ApiResponse<GraphData>>

    // ─── Admin ────────────────────────────────────────────────────────────────

    @GET("admin/dashboard")
    suspend fun getAdminDashboard(
        @Header("Authorization") token: String
    ): Response<ApiResponse<DashboardStats>>

    @GET("admin/reports")
    suspend fun getAdminReports(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedApiResponse<AdminReportItem>>

    @PUT("admin/reports/{id}/verify")
    suspend fun verifyReport(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: ReviewRequest = ReviewRequest()
    ): Response<ApiResponse<AdminReportItem>>

    @PUT("admin/reports/{id}/reject")
    suspend fun rejectReport(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: ReviewRequest = ReviewRequest()
    ): Response<ApiResponse<AdminReportItem>>

    @PUT("admin/reports/{id}/reviewing")
    suspend fun markReviewing(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<AdminReportItem>>
}
