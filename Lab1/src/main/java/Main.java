import lombok.SneakyThrows;
import model.Bill;
import model.Product;
import model.Sales;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static int NO_THREADS = 20;
    public static volatile boolean SHOULD_TERMINATE = false;
    /*
    1. Supermarket inventory:
    There are several types of products, each having a known, constant, unit price.
    In the beginning, we know the quantity of each product.

    We must keep track of the quantity of each product, the amount of money (initially zero)
    , and the list of bills, corresponding to sales. Each bill is a list of items and
    quantities sold in a single operation, and their total price.

    We have sale operations running concurrently, on several threads. Each sale decreases
     the amounts of available products (corresponding to the sold items), increases the
     amount of money, and adds a bill to a record of all sales.

    From time to time, as well as at the end, an inventory check operation shall be run.
    It shall check that all the sold products and all the money are justified by the
    recorded bills.
         */
    @SneakyThrows
    public static void main(String[] args) {
        runAutomatedSale();
        List<Product> products = new ArrayList<>();
        HashMap<String, Product> originals = new HashMap<>();
        System.out.println("Enter the list of products.\nYou can stop by pressing x");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int totalStock = 0;
        while (true){
            String name, token;
            System.out.println("Enter product name");
            token = reader.readLine();
            if (token.toLowerCase().equals("x"))
                break;
            name = token;
            System.out.println("Enter price for the product");
            Integer price = Integer.parseInt(reader.readLine());
            System.out.println("Enter stock for the product");
            Integer stock = Integer.parseInt(reader.readLine());
            if(price < 1 || stock < 1){
                System.out.println("Failed to create product");
            }
            else{
                Product product = new Product(name, price, stock, new ReentrantLock());
                products.add(product);
                totalStock += stock;
                originals.put(name, new Product(name, price, stock, null));
            }
        }

        Sales sales = new Sales();
        // start creating threads
        Thread inventoryThread = new Thread(() -> {
            while (!Main.SHOULD_TERMINATE){
               try {
                   System.out.println(Main.SHOULD_TERMINATE);
                   sales.doInventory(originals);
                   Thread.sleep(100);
               }
               catch (InterruptedException e){
                   System.out.println("Something went wrong in Thread.sleep");
               }
            }
        });

        List<Thread> threads = new ArrayList<>();
        for(int index=1; index<= Main.NO_THREADS; index+=1){
            Random random = new Random();
            int noItems = random.nextInt(totalStock/Main.NO_THREADS - 1) + 1;
            int finalIndex = index;
            Thread t = new Thread(() -> {
                if(!Main.SHOULD_TERMINATE)
                      Main.generateBill(noItems, sales, products, finalIndex, true);
            });
            threads.add(t);
            totalStock -= noItems;
        }
        inventoryThread.start();
        for(Thread t: threads){
            t.start();
        }
        System.out.println("give smth");
        int s = reader.read();
        if(s>=0)
            Main.SHOULD_TERMINATE = true;
        for(Thread t: threads){
            t.join();
        }
        inventoryThread.join();
        System.out.println("\n<<<THREADS FINISHED>>>\n");
        sales.doInventory(originals);

    }

    @SneakyThrows
    public static void runAutomatedSale(){
        String s = "q,w,e,r,t,y,u,i,o,p,a,s,d,f,g,h,j,k,l,z,x,c,v,b,n,m,qw,we,er,rt,ty,yu,ui,io,op,as,df,fg,gh,hj,jk,kl"+
                "zx,xc,cv,vb,bn,nm,qwe,wer,ert,rty,tyu,yui,uio,iop,asd,sdf,dfg,fgh,ghj,jkl,zxc,xcv,cvb,vbn,bnm,Q,W,E,R,T,Y"+
                "U,I,O,P,A,S,D,F,G,H,J,K,L,M,N,B,V,C,X,Z,QW,WE,ER,RT,YU,UI,IO,AS,DS,DF,FG,GH,HJ,JK,KL,ZX,XC,CV,VB,BN,NM";
        List<Product> products = new ArrayList<>();
        HashMap<String, Product> originals = new HashMap<>();
        int totalStock = 0;
        for(String prod: s.split(",")) {
            Random random = new Random();
            int price = random.nextInt(50) + 1;
            int quant = random.nextInt(100000) + 1;
            totalStock += quant;
            products.add(new Product(prod, price, quant, new ReentrantLock()));
            originals.put(prod, new Product(prod, price, quant, null));
        }
        Sales sales = new Sales();
        // start creating threads
        List<Thread> threads = new ArrayList<>();
        for(int index=1; index<= Main.NO_THREADS; index+=1){
            Random random = new Random();
            int noItems;
            if(totalStock > 1)
               noItems = random.nextInt(totalStock - 1) + 1;
            else
                noItems = totalStock;
            int finalIndex = index;
            Thread t = new Thread(() -> {
                Main.generateBill(noItems, sales, products, finalIndex, false);
            });
            t.start();
            threads.add(t);
            totalStock -= noItems;
        }
        for(Thread t: threads){
            t.join();
        }
        System.out.println("\n<<<THREADS FINISHED>>>\n");

        sales.doInventory(originals);
    }

    public static void generateBill(int noItems, Sales sales, List<Product> products, int id, boolean use){
        System.out.println("Thread "+id+" has started");
        Bill bill = new Bill();
        int noTrials = 100000;
        int current = 0;
        while(noItems > 0 && noTrials > current && !Main.SHOULD_TERMINATE){
            Random random = new Random();
            int toBeBought = noItems>1 ? random.nextInt(noItems - 1) + 1 : 1;
            Product selectedProduct = products.get(random.nextInt(products.size())); // select random elem from list
            boolean res = bill.buyProduct(selectedProduct, toBeBought);
            if (res)
                noItems -= toBeBought;
            else{
                int estimatedStock = selectedProduct.getQuantity();
                toBeBought = estimatedStock> 1 ? random.nextInt(estimatedStock+1)+1 : 1;
                res = bill.buyProduct(selectedProduct, toBeBought);
                if(res)
                    noItems -= toBeBought;
            }
            current++;
        }
        sales.addBill(bill);
        if(use) {
            try {
                Thread.sleep(new Random().nextInt(6000) + 1000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Thread "+id+" has finished\n"+"Bill is  \n"+bill);
    }



}
