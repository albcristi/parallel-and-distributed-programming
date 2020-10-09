package model;

import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    private List<Item> products = new ArrayList<>();

    private Integer total = 0;

    // we use a lock when we want to access the
    // products list. E.g. of operations that needs
    // to be synchronized: adding a new product
    private ReentrantLock productsLock = new ReentrantLock();

    // we use another lock for the total of the Bill
    private ReentrantLock totalLock = new ReentrantLock();

    public boolean buyProduct(Product product, Integer quantity){
        boolean result;
        result = product.decreaseQuantity(quantity);
        if(result){
            productsLock.lock();
            try {
                Item it = Item.builder()
                        .name(product.getName())
                        .quantity(quantity)
                        .price(product.getPrice()*quantity)
                        .build();
                result = false;
                for(Item item: products)
                    if(item.equals(it)){
                        item.setQuantity(item.getQuantity()+quantity);
                        item.setPrice(item.getPrice()+quantity*product.getPrice());
                        result = true;
                    }
                if(!result)
                   products.add(it);
            }
            finally {
                productsLock.unlock();
            }

            totalLock.lock();
            this.total += product.getPrice()*quantity;
            totalLock.unlock();
            return true;
        }
        else
            return false;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "products=" + products +
                ", total=" + total +
                '}';
    }
}
