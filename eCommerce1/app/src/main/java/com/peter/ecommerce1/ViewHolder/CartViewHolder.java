package com.peter.ecommerce1.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peter.ecommerce1.Interface.ItemClickListener;
import com.peter.ecommerce1.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public TextView productNameText, productQuantityText, productPriceText;
    private ItemClickListener itemClickListener;

    public CartViewHolder(@NonNull View itemView)
    {
        super(itemView);

        productNameText = (TextView) itemView.findViewById(R.id.cart_item_product_name);
        productQuantityText = (TextView) itemView.findViewById(R.id.cart_item_product_quantity);
        productPriceText = (TextView) itemView.findViewById(R.id.cart_item_product_price);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
