package com.peter.ecommerce1.ViewHolder;

import android.content.DialogInterface;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peter.ecommerce1.Interface.ItemClickListener;
import com.peter.ecommerce1.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public TextView productNameText, productDescriptionText,productPrice;
    public ImageView productImageView;

    public ItemClickListener listener;


    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        productNameText =(TextView) itemView.findViewById(R.id.home_product_name);
        productDescriptionText = (TextView) itemView.findViewById(R.id.home_product_description);
        productPrice = (TextView) itemView.findViewById(R.id.home_product_price);

        productImageView = (ImageView) itemView.findViewById(R.id.home_product_image);
    }

    public void setItemClickListener(ItemClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {

        listener.onClick(view,getAdapterPosition(),false);
    }
}
