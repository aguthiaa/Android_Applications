package com.peter.ecommerce1.Model;

public class Cart
{
    private String pid,productName, quantity, productPrice,discount;

    public Cart()
    {

    }

    public Cart(String pid, String productName, String quantity, String productPrice, String discount) {
        this.pid = pid;
        this.productName = productName;
        this.quantity = quantity;
        this.productPrice = productPrice;
        this.discount = discount;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
