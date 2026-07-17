package com.garuda.floatingbubble.data

import com.google.gson.annotations.SerializedName

// ─── Auth ─────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val password: String
)

data class UserInfo(
    val id: Int,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val role: String
)

data class AuthData(
    val token: String,
    val user: UserInfo
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData
)

// ─── Generic API response wrapper ─────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class PaginatedApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: PaginatedData<T>?
)

data class PaginatedData<T>(
    val items: List<T>,
    val pagination: Pagination
)

data class Pagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

// ─── Report ───────────────────────────────────────────────────────────────────

data class EntityInput(
    @SerializedName("entity_value") val entityValue: String,
    @SerializedName("entity_type") val entityType: String
)

data class SubmitReportRequest(
    val entities: List<EntityInput>,
    val category: String,
    val description: String,
    @SerializedName("proof_image") val proofImage: String
)

data class ReportEntityItem(
    val id: Int,
    val type: String,
    val value: String,
    val category: String?,
    val status: String,
    @SerializedName("risk_score") val riskScore: Int
)

data class ReportItem(
    val id: Int,
    val status: String,
    val category: String,
    val description: String,
    @SerializedName("proof_image") val proofImage: String?,
    @SerializedName("review_note") val reviewNote: String?,
    @SerializedName("created_at") val createdAt: String,
    val entities: List<ReportEntityItem>
)

// ─── Admin Report ─────────────────────────────────────────────────────────────

data class AdminReportUser(
    val id: Int,
    val email: String,
    @SerializedName("full_name") val fullName: String
)

data class AdminReportItem(
    val id: Int,
    val status: String,
    val category: String,
    val description: String,
    @SerializedName("proof_image") val proofImage: String?,
    @SerializedName("review_note") val reviewNote: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("reviewed_at") val reviewedAt: String?,
    val user: AdminReportUser?,
    val entities: List<ReportEntityItem>
)

data class ReviewRequest(
    @SerializedName("review_note") val reviewNote: String = ""
)

data class DashboardStats(
    @SerializedName("total_reports") val totalReports: Int,
    @SerializedName("by_status") val byStatus: ByStatus,
    @SerializedName("total_entities") val totalEntities: Int,
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("reports_today") val reportsToday: Int
)

data class ByStatus(
    val pending: Int,
    val reviewing: Int,
    val verified: Int,
    val rejected: Int
)

// ─── Check / Scan ─────────────────────────────────────────────────────────────

data class CheckRequest(
    val value: String,
    val type: String? = null
)

data class CheckResult(
    @SerializedName("entity_type") val entityType: String,
    @SerializedName("entity_value") val entityValue: String,
    val verdict: String,
    val status: String,
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("confidence_score") val confidenceScore: Float,
    @SerializedName("report_count") val reportCount: Int,
    val reason: String
)

data class CheckImageResponse(
    val summary: CheckResult,
    val entities: List<CheckResult>
)

// ─── Entity (Database menu) ───────────────────────────────────────────────────

data class EntityItem(
    val id: Int,
    val type: String,
    val value: String,
    val category: String?,
    val status: String,
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("report_count") val reportCount: Int
)

// ─── Graph ────────────────────────────────────────────────────────────────────

data class GraphNode(
    val id: String,
    val type: String,
    val value: String,
    val category: String?,
    val status: String,
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("report_count") val reportCount: Int
)

data class GraphEdge(
    @SerializedName("source_id") val sourceId: String,
    @SerializedName("target_id") val targetId: String,
    @SerializedName("relation_type") val relationType: String
)

data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)
