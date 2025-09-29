package io.github.stcksmsh.pravnik.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.domain.model.Bookmark
import io.github.stcksmsh.pravnik.domain.model.Document
import io.github.stcksmsh.pravnik.domain.model.History
import io.github.stcksmsh.pravnik.domain.model.SearchQuery

class SimpleTextAdapter<T>(private val binder: (T) -> String, private val onClick: (T) -> Unit) : ListAdapter<T, SimpleTextAdapter<T>.VH>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem === newItem
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
}) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.title)
        fun bind(item: T) { tv.text = binder(item); itemView.setOnClickListener { onClick(item) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_home_row, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}

// Convenience binders
object HomeBinders {
    val query: (SearchQuery) -> String = { it.query }
    val bookmark: (Bookmark) -> String = { it.title ?: it.unitAnchor }
    val history: (History) -> String = { "${'$'}{it.docId}#${'$'}{it.unitAnchor}" }
    val document: (Document) -> String = { it.title }
}
