package model;

import lombok.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


@Getter
@Setter
@ToString
public class Sales {
    private List<Bill> sales = new ArrayList<>();

    private ReentrantLock salesLock = new ReentrantLock();

    public Sales(){

    }

    public void addBill(Bill bill){
        salesLock.lock();
        try{
            sales.add(bill);
        }
        finally {
            salesLock.unlock();
        }
    }

    public void doInventory(HashMap<String, Product> originals){
        salesLock.lock();
        try{
            System.out.println("INVENTORY STARTED");
            boolean failed = false;
            for(Bill bill: sales){
                int money = 0;
                List<Item> items = bill.getProducts();
                for(Item it: items)
                    if(it.getPrice() != originals.get(it.getName()).getPrice()*it.getQuantity())
                        failed = true;
            }
            for(String prodName: originals.keySet()){
                int boughtItems = 0;
                for(Bill bill: sales){
                    for(Item item: bill.getProducts())
                        if(item.getName().equals(prodName))
                            boughtItems += item.getQuantity();
                }
                if(boughtItems > originals.get(prodName).getQuantity())
                    failed = true;
            }
            if(failed)
                System.out.println("INVENTORY DETECTED SOME FAULTY DATA");
            else
                System.out.println("INVENTORY FINISHED WITH NO DETECTED ERRORS");
        }
        finally {
            salesLock.unlock();
        }
    }
}
