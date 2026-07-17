package com.garuda.floatingbubble.data

data class ReportData(
    val id: Int,
    val filledEntities: List<Pair<String, String>>,
    val category: String,
    val description: String,
    val timestamp: Long,
    var status: String = "PENDING"
) {
    val entities: List<String> get() = filledEntities.map { it.second }
}

data class EntityData(
    val id: String, // same as value for simplicity in mock
    val type: String,
    val category: String,
    var riskScore: Int = 100
)

data class EntityRelation(
    val sourceId: String,
    val targetId: String,
    val type: String = "terkait_laporan_sama"
)

object MockRepository {
    private val reports = mutableListOf<ReportData>()
    private val entities = mutableMapOf<String, EntityData>()
    private val relations = mutableListOf<EntityRelation>()

    init {
        // Populate initial mock data
        addMockEntity("081234567890", "phone", "Penipuan Online", 85)
        addMockEntity("BCA 12345678", "bank_account", "Penipuan Online", 95)
        addMockEntity("MANDIRI 9876", "bank_account", "Investasi Bodong", 70)
        addMockEntity("http://bit.ly/undian", "url", "Phishing", 90)
        addMockEntity("089999999", "phone", "Pinjol Ilegal", 80)
        
        // Initial relations
        relations.add(EntityRelation("081234567890", "BCA 12345678"))
        relations.add(EntityRelation("MANDIRI 9876", "http://bit.ly/undian"))
    }

    private fun addMockEntity(value: String, type: String, category: String, score: Int) {
        entities[value] = EntityData(value, type, category, score)
    }

    fun addReport(filledEntities: List<Pair<String, String>>, category: String, description: String) {
        if (filledEntities.isEmpty()) return
        
        // 1. Save Report as PENDING (Do not add to entities or graph yet)
        val report = ReportData(
            id = reports.size + 1,
            filledEntities = filledEntities,
            category = category,
            description = description,
            timestamp = System.currentTimeMillis(),
            status = "PENDING"
        )
        reports.add(0, report) // Add to top
    }

    fun verifyReport(reportId: Int) {
        val report = reports.find { it.id == reportId } ?: return
        if (report.status != "PENDING") return

        report.status = "VERIFIED"

        // 2. Add or Update Entities
        for ((type, value) in report.filledEntities) {
            if (!entities.containsKey(value)) {
                entities[value] = EntityData(value, type, report.category, 100)
            }
        }

        // 3. Create Relations (link all entities in this report together)
        if (report.filledEntities.size > 1) {
            for (i in report.filledEntities.indices) {
                for (j in i + 1 until report.filledEntities.size) {
                    val source = report.filledEntities[i].second
                    val target = report.filledEntities[j].second
                    
                    // Check if relation already exists (undirected)
                    val exists = relations.any { 
                        (it.sourceId == source && it.targetId == target) || 
                        (it.sourceId == target && it.targetId == source)
                    }

                    if (!exists) {
                        relations.add(EntityRelation(source, target))
                    }
                }
            }
        }
    }

    fun rejectReport(reportId: Int) {
        val report = reports.find { it.id == reportId } ?: return
        if (report.status == "PENDING") {
            report.status = "REJECTED"
        }
    }

    fun getReports(): List<ReportData> = reports.toList()

    fun getEntities(): List<EntityData> = entities.values.toList()

    fun getRelations(): List<EntityRelation> = relations.toList()
}
