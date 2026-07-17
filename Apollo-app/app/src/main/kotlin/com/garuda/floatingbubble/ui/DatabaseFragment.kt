package com.garuda.floatingbubble.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garuda.floatingbubble.R
import com.garuda.floatingbubble.data.ApiConfig
import com.garuda.floatingbubble.data.EntityItem
import com.garuda.floatingbubble.data.GraphEdge
import com.garuda.floatingbubble.data.GraphNode
import com.garuda.floatingbubble.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseFragment : Fragment() {

    private lateinit var rvDatabase: RecyclerView
    private lateinit var etSearchDatabase: EditText
    private lateinit var adapter: EntityAdapter

    private lateinit var btnFilterAll: android.widget.TextView
    private lateinit var btnFilterPhone: android.widget.TextView
    private lateinit var btnFilterBank: android.widget.TextView
    private lateinit var btnFilterUrl: android.widget.TextView
    private lateinit var btnFilterEmail: android.widget.TextView
    private var currentType: String? = null

    private var allEntities: List<EntityItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_database, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDatabase = view.findViewById(R.id.rvDatabase)
        etSearchDatabase = view.findViewById(R.id.etSearchDatabase)
        btnFilterAll = view.findViewById(R.id.btnFilterAll)
        btnFilterPhone = view.findViewById(R.id.btnFilterPhone)
        btnFilterBank = view.findViewById(R.id.btnFilterBank)
        btnFilterUrl = view.findViewById(R.id.btnFilterUrl)
        btnFilterEmail = view.findViewById(R.id.btnFilterEmail)

        setupRecyclerView()
        setupSearchAndFilter()
        setupToggleAndGraph(view)
        loadEntities()
    }

    override fun onResume() {
        super.onResume()
        loadEntities()
    }

    private fun loadEntities() {
        val view = view ?: return
        val pbLoading = view.findViewById<ProgressBar?>(R.id.pbDatabaseLoading)
        pbLoading?.visibility = View.VISIBLE

        val token = SessionManager.bearerToken(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getEntities(token = token, limit = 200)
                withContext(Dispatchers.Main) {
                    pbLoading?.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        allEntities = response.body()?.data?.items ?: emptyList()
                        adapter.updateData(allEntities)
                        // Refresh graph if visible
                        val wvGraph = view?.findViewById<android.webkit.WebView>(R.id.wvGraph)
                        if (wvGraph != null && wvGraph.visibility == View.VISIBLE) {
                            loadGraphData(wvGraph)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat database", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbLoading?.visibility = View.GONE
                    Toast.makeText(requireContext(), "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = EntityAdapter(emptyList())
        rvDatabase.layoutManager = LinearLayoutManager(requireContext())
        rvDatabase.adapter = adapter
    }

    private fun setupSearchAndFilter() {
        etSearchDatabase.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString(), currentType)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val buttons = listOf(btnFilterAll, btnFilterPhone, btnFilterBank, btnFilterUrl, btnFilterEmail)

        fun selectButton(selectedBtn: android.widget.TextView, type: String?) {
            currentType = type
            buttons.forEach { it.isSelected = (it == selectedBtn) }
            adapter.filter(etSearchDatabase.text.toString(), currentType)
        }

        btnFilterAll.setOnClickListener { selectButton(btnFilterAll, null) }
        btnFilterPhone.setOnClickListener { selectButton(btnFilterPhone, "phone") }
        btnFilterBank.setOnClickListener { selectButton(btnFilterBank, "bank_account") }
        btnFilterUrl.setOnClickListener { selectButton(btnFilterUrl, "url") }
        btnFilterEmail.setOnClickListener { selectButton(btnFilterEmail, "email") }

        selectButton(btnFilterAll, null)
    }

    private fun setupToggleAndGraph(view: View) {
        val btnToggleList: android.widget.TextView = view.findViewById(R.id.btnToggleList)
        val btnToggleGraph: android.widget.TextView = view.findViewById(R.id.btnToggleGraph)
        val wvGraph: android.webkit.WebView = view.findViewById(R.id.wvGraph)
        val svFilterGroup: android.widget.HorizontalScrollView = view.findViewById(R.id.svFilterGroup)

        wvGraph.settings.javaScriptEnabled = true
        wvGraph.settings.domStorageEnabled = true

        btnToggleList.isSelected = true
        btnToggleGraph.isSelected = false

        btnToggleList.setOnClickListener {
            btnToggleList.isSelected = true
            btnToggleGraph.isSelected = false
            rvDatabase.visibility = View.VISIBLE
            wvGraph.visibility = View.GONE
            svFilterGroup.visibility = View.VISIBLE
            etSearchDatabase.visibility = View.VISIBLE
        }

        btnToggleGraph.setOnClickListener {
            btnToggleGraph.isSelected = true
            btnToggleList.isSelected = false
            rvDatabase.visibility = View.GONE
            wvGraph.visibility = View.VISIBLE
            svFilterGroup.visibility = View.GONE
            etSearchDatabase.visibility = View.GONE
            loadGraphData(wvGraph)
        }

        wvGraph.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadGraphData(wvGraph)
            }
        }
        wvGraph.loadUrl("file:///android_asset/graph.html")
    }

    private fun loadGraphData(webView: android.webkit.WebView) {
        val token = SessionManager.bearerToken(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getGraph(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val graphData = response.body()?.data
                        injectGraphData(webView, graphData?.nodes ?: emptyList(), graphData?.edges ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                // Graph data unavailable — silently skip
            }
        }
    }

    private fun injectGraphData(webView: android.webkit.WebView, nodes: List<GraphNode>, edges: List<GraphEdge>) {
        val nodesList = nodes.map { node ->
            val color = when (node.type) {
                "phone" -> "#F4A261"
                "bank_account" -> "#E36954"
                "email" -> "#2A9D8F"
                "url" -> "#E63946"
                else -> "#88A8DADC"
            }
            // Use node.id (numeric string) as identifier but display node.value
            "{\"id\": \"${node.id}\", \"label\": \"${node.value.replace("\"", "\\\"")}\", \"type\": \"${node.type}\", \"color\": \"$color\"}"
        }
        val nodesJson = "[${nodesList.joinToString(",")}]"

        val edgesList = edges.map { edge ->
            "{\"from\": \"${edge.sourceId}\", \"to\": \"${edge.targetId}\"}"
        }
        val edgesJson = "[${edgesList.joinToString(",")}]"

        val jsCode = "javascript:loadGraphData('$nodesJson', '$edgesJson')"
        webView.evaluateJavascript(jsCode, null)
    }
}
