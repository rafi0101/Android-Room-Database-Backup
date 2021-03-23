package com.ebner.roomdatabasebackup.sample.database.table.fruit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ebner.roomdatabasebackup.sample.R

/**
 *  MIT License
 *
 *  Copyright (c) 2021 Raphael Ebner
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
class FruitListAdapter(private val itemClickListener: OnItemClickListener) :
    ListAdapter<Fruit, FruitListAdapter.FruitViewHolder>(TaskDiffCallback()) {


    /*---------------------creates the ViewHolder (returns the view with all items in it)--------------------------*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FruitViewHolder {
        return FruitViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.content_fruit_list, parent, false)
        )
    }

    /*---------------------Bind the data with the View--------------------------*/
    override fun onBindViewHolder(holder: FruitViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }

    /*---------------------Transform Fruit infos to position number--------------------------*/
    fun getFruitAt(position: Int): Fruit? {
        return getItem(position)
    }


    /*---------------------Creates an onClickListener (when you press on a item, you get the ID, and can do what ever you want--------------------------*/
    interface OnItemClickListener {

        fun onItemClicked(fruit: Fruit)
    }

    /*---------------------get the item from the onBindViewHolder, and apply it to the current view row--------------------------*/
    inner class FruitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Fruit, itemclickListener: OnItemClickListener) = with(itemView) {
            //Bind the data with View
            val tvFruitName: TextView = itemView.findViewById(R.id.tv_fruit_name)

            tvFruitName.text = item.name

            itemView.setOnClickListener {
                itemclickListener.onItemClicked(item)
            }
        }
    }

}

/*---------------------Makes the Animation to the recyclerview, when item is changed, added or deleted--------------------------*/
class TaskDiffCallback : DiffUtil.ItemCallback<Fruit>() {
    override fun areItemsTheSame(oldItem: Fruit, newItem: Fruit): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Fruit, newItem: Fruit): Boolean {
        //Compare all items, so if there is a new field, add it with &&
        return oldItem.name == newItem.name
    }


}