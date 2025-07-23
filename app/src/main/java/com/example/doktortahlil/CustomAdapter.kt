package com.example.doktortahlil

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doktortahlil.Item
import com.example.doktortahlil.R
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import android.util.Log
import android.util.SparseBooleanArray
import android.util.SparseArray

data class ArrowState(
    var isUpSelected: Boolean = false,
    var isDownSelected: Boolean = false
)

class CustomAdapter(
    private var itemList: MutableList<Item>,
    private val context: Context
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private val arrowStates = mutableMapOf<String, ArrowState>()
    private val originalItemList: List<Item> = itemList.toList() // Make a copy of itemList for originalItemList

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewDown: ImageView = itemView.findViewById(R.id.imageView)
        val imageViewUp: ImageView = itemView.findViewById(R.id.imageView2)
        val tahlilTextView: TextView = itemView.findViewById(R.id.Tahlil)
        val refRangeTextView: TextView = itemView.findViewById(R.id.ref_range)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.tahlilTextView.text = item.Tahlil
        holder.refRangeTextView.text = item.ref_range

        // Get or create the arrow state for the current item based on Tahlil
        val arrowState = arrowStates[item.Tahlil] ?: ArrowState().also { arrowStates[item.Tahlil] = it }

        // Set the visual state based on the saved state
        holder.imageViewDown.setImageResource(
            if (arrowState.isDownSelected) R.drawable.red_arrow_down_icon_png_30 else R.drawable.red_arrow_down_icon_png_30
        )
        holder.imageViewUp.setImageResource(
            if (arrowState.isUpSelected) R.drawable.red_arrow_up_icon_png_30 else R.drawable.red_arrow_up_icon_png_30
        )

        // Clear halo effect
        holder.imageViewDown.background = null
        holder.imageViewUp.background = null

        // Apply halo effect if selected
        if (arrowState.isDownSelected) {
            applyHaloEffect(holder.imageViewDown)
        } else if (arrowState.isUpSelected) {
            applyHaloEffect(holder.imageViewUp)
        }

        holder.imageViewDown.setOnClickListener {
            Log.d("CustomAdapter", "Down arrow clicked: Tahlil ${item.Tahlil}")
            val isSelected = !arrowState.isDownSelected
            arrowState.isDownSelected = isSelected
            arrowState.isUpSelected = false // Unselect the up arrow if the down arrow is selected

            item.isDownArrowSelected = isSelected
            item.isUpArrowSelected = false

            if (isSelected) {
                applyClickedAnimation(holder.imageViewDown)
                applyHaloEffect(holder.imageViewDown)
            }

            notifyItemChanged(position)
        }

        holder.imageViewUp.setOnClickListener {
            Log.d("CustomAdapter", "Up arrow clicked: Tahlil ${item.Tahlil}")
            val isSelected = !arrowState.isUpSelected
            arrowState.isUpSelected = isSelected
            arrowState.isDownSelected = false // Unselect the down arrow if the up arrow is selected

            item.isUpArrowSelected = isSelected
            item.isDownArrowSelected = false

            if (isSelected) {
                applyClickedAnimation(holder.imageViewUp)
                applyHaloEffect(holder.imageViewUp)
            }

            notifyItemChanged(position)
        }
    }

    private fun applyClickedAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.click_animation)
        view.startAnimation(animation)
    }

    private fun applyHaloEffect(imageView: ImageView) {
        val haloDrawable = ContextCompat.getDrawable(imageView.context, R.drawable.halo_shape)
        imageView.background = haloDrawable
    }

    fun getMessages(): List<String> {
        return itemList.filter {
            it.isUpArrowSelected || it.isDownArrowSelected
        }.map {
            if (it.isUpArrowSelected) it.high_message else it.low_message
        }
    }

    fun clearMessages() {
        itemList.forEach {
            it.isUpArrowSelected = false
            it.isDownArrowSelected = false
        }
        arrowStates.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun filterItems(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalItemList
        } else {
            originalItemList.filter { item ->
                item.Tahlil.contains(query, ignoreCase = true)
            }
        }

        updateItemList(filteredList)
    }

    fun updateItemList(newItemList: List<Item>) {
        itemList.clear()
        itemList.addAll(newItemList)
        notifyDataSetChanged()
    }
}
