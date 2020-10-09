package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Product {
    // the name of the product
    private String name;

    // the price of the product
    private final Integer price;

    // represents the available quantity that we
    // have in stock for a product
    // Observation:
    // we could also use the AtomicInteger class
    // that will assure the atomicity of the operations
    // performed for the quantity object
    private Integer quantity;

    // we assure the synchronization of operations on
    // the quantity field by using a ReentrantLock
    private ReentrantLock quantityLock;

    public void setPrice(Integer price){
        throw new RuntimeException("Price can not be changed for an object");
    }

    // we can assure the synchronization also by using the
    // 'synchronized' keyword when declaring the method
    public boolean decreaseQuantity(Integer amount){
        boolean isOk = true;
        //sync mechanism
        this.quantityLock.lock();
        this.quantity = this.quantity - amount;
        if(this.quantity <= 0) {
            isOk = false;
            this.quantity=0;
        }
        this.quantityLock.unlock();
        return isOk;
    }

    public Integer getQuantity(){
        return this.quantity;
    }

}
