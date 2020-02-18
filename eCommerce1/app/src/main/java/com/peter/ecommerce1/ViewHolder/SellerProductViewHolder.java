package com.peter.ecommerce1.ViewHolder;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peter.ecommerce1.Interface.ItemClickListener;
import com.peter.ecommerce1.R;

public class SellerProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public TextView sName, sPrice, sDescription,sStatus;
    public ImageView sImage;
    public ItemClickListener listener;

    public SellerProductViewHolder(@NonNull View itemView)
    {
        super(itemView);

        sName = itemView.findViewById(R.id.seller_product_name);
        sPrice = itemView.findViewById(R.id.seller_product_price);
        sDescription= itemView.findViewById(R.id.seller_product_description);
        sStatus = itemView.findViewById(R.id.seller_product_status);
        sImage = itemView.findViewById(R.id.seller_product_image);
    }

    public void setItemClickListener(ItemClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View view)
    {
        listener.onClick(view,getAdapterPosition(),false);
    }
}
