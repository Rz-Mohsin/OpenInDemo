package com.example.openindemo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.openindemo.databinding.ItemLinkListBinding
import com.example.openindemo.models.Link

class LinkListAdapter(val context : Context) : RecyclerView.Adapter<LinkListAdapter.LinkListViewHolder>()  {
    inner class LinkListViewHolder(val binding : ItemLinkListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : Link){
            binding.apply {
                tvClicksCount.text = item.total_clicks.toString()
                tvLinkName.text = item.title
                tvLinkCreationTime.text = item.times_ago
                tvSmartLink.text = item.smart_link
                val imageUrl = item.thumbnail
                if(imageUrl!=null){
                    Glide.with(context)
                        .load(imageUrl)
                        .into(ivLinkThumbNail)
                }
            }
        }
    }

    private val differCallBack = object : DiffUtil.ItemCallback<Link>(){
        override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean {
            return oldItem.url_id == newItem.url_id
        }

        override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkListViewHolder {
        val view = ItemLinkListBinding.inflate(LayoutInflater.from(parent.context))
        val layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = 30
        view.root.layoutParams = layoutParams
        return LinkListViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkListViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}