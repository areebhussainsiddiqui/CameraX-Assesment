package com.ahs.camerax.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ahs.camerax.Model.PhotoModel
import com.ahs.camerax.R
import com.ahs.camerax.databinding.ItemImageBinding

class PhotosAdapter (private val photoModel:List<PhotoModel>, private val clickListener:(PhotoModel) -> Unit):
    RecyclerView.Adapter<PhotosAdapter.MyViewHolder>(){


    class MyViewHolder(val binding:ItemImageBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(photoModel: PhotoModel,clickListener: (PhotoModel) -> Unit){
            binding.image.setImageBitmap(photoModel.bitmap)
            binding.LayoutItem.setOnClickListener {
                clickListener(photoModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding:ItemImageBinding = DataBindingUtil.
        inflate(layoutInflater,
            R.layout.item_image,
            parent,
            false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return photoModel.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(photoModel[position],clickListener);
    }
}