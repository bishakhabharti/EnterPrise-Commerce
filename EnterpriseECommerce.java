import java.util.*;
import java.util.concurrent.*;

// ================= DATABASE (Singleton Pattern) =================

class Database {

    private static Database instance;

    private Map<Integer, Product> products;

    private Database() {

        products = new HashMap<>();

        products.put(1,new Product(1,"Laptop",75000));
        products.put(2,new Product(2,"Mouse",1200));
        products.put(3,new Product(3,"Keyboard",2500));
        products.put(4,new Product(4,"Phone",50000));
    }

    public static Database getInstance() {

        if(instance == null)
            instance = new Database();

        return instance;
    }

    public Product getProduct(int id) {

        return products.get(id);
    }

    public Collection<Product> getAllProducts() {

        return products.values();
    }
}


// ================= PRODUCT MODEL =================

class Product {

    private int id;
    private String name;
    private double price;

    private DiscountStrategy discountStrategy;

    public Product(int id,String name,double price) {

        this.id=id;
        this.name=name;
        this.price=price;
    }

    public void setDiscountStrategy(
            DiscountStrategy strategy) {

        this.discountStrategy=strategy;
    }

    public double getFinalPrice() {

        if(discountStrategy==null)
            return price;

        return discountStrategy.applyDiscount(price);
    }

    public int getId(){return id;}

    public String getName(){return name;}

    public double getPrice(){return price;}
}


// ================= STRATEGY PATTERN =================

interface DiscountStrategy {

    double applyDiscount(double price);
}

class PercentageDiscount
        implements DiscountStrategy {

    private double percent;

    public PercentageDiscount(double percent){

        this.percent=percent;
    }

    public double applyDiscount(double price){

        return price - (price*percent/100);
    }
}


// ================= FACTORY PATTERN =================

interface PaymentProcessor {

    void processPayment(double amount);
}

class CreditCardProcessor
        implements PaymentProcessor {

    public void processPayment(double amount){

        System.out.println(
        "Credit Card Payment Successful: ₹"+amount);
    }
}

class PayPalProcessor
        implements PaymentProcessor {

    public void processPayment(double amount){

        System.out.println(
        "PayPal Payment Successful: ₹"+amount);
    }
}

class PaymentFactory {

    public static PaymentProcessor
    getPaymentProcessor(String type){

        if(type.equalsIgnoreCase("card"))
            return new CreditCardProcessor();

        else if(type.equalsIgnoreCase("paypal"))
            return new PayPalProcessor();

        else
            throw new RuntimeException("Invalid payment type");
    }
}


// ================= OBSERVER PATTERN =================

interface OrderObserver {

    void update(String status);
}

class EmailNotifier
        implements OrderObserver {

    public void update(String status){

        System.out.println(
        "Email Notification: Order "+status);
    }
}

class SMSNotifier
        implements OrderObserver {

    public void update(String status){

        System.out.println(
        "SMS Notification: Order "+status);
    }
}


// ================= ORDER MODEL =================

class Order {

    private List<Product> products;

    private List<OrderObserver> observers;

    private String status;

    public Order(){

        products=new ArrayList<>();
        observers=new ArrayList<>();
    }

    public void addProduct(Product p){

        products.add(p);
    }

    public void addObserver(OrderObserver o){

        observers.add(o);
    }

    public double getTotal(){

        double sum=0;

        for(Product p:products)
            sum+=p.getFinalPrice();

        return sum;
    }

    public void setStatus(String status){

        this.status=status;

        notifyObservers();
    }

    private void notifyObservers(){

        for(OrderObserver o:observers)
            o.update(status);
    }
}


// ================= PRODUCT SERVICE =================

class ProductService {

    Database db = Database.getInstance();

    public void displayProducts(){

        System.out.println("\nAvailable Products:");

        for(Product p:db.getAllProducts()){

            System.out.println(
            p.getId()+" | "+
            p.getName()+" | ₹"+
            p.getPrice());
        }
    }

    public Product getProduct(int id){

        return db.getProduct(id);
    }
}


// ================= ORDER SERVICE =================

class OrderService {

    ExecutorService executor =
    Executors.newFixedThreadPool(2);

    public void placeOrder(Order order,
                           String paymentType){

        executor.submit(() -> {

            try{

                System.out.println(
                "\nProcessing order...");

                Thread.sleep(2000);

                double total=order.getTotal();

                PaymentProcessor processor =
                PaymentFactory.getPaymentProcessor(paymentType);

                processor.processPayment(total);

                order.setStatus("COMPLETED");

                System.out.println(
                "Order placed successfully!");

            }
            catch(Exception e){

                System.out.println(
                "Order failed: "+e.getMessage());
            }
        });
    }

    public void shutdown(){

        executor.shutdown();
    }
}


// ================= VALIDATOR =================

class Validator {

    public static boolean validateProduct(Product p){

        return p!=null;
    }
}


// ================= MAIN CLASS =================

public class EnterpriseECommerce {

    public static void main(String[] args) {

        Scanner sc=new Scanner(System.in);

        ProductService productService =
        new ProductService();

        OrderService orderService =
        new OrderService();

        productService.displayProducts();

        System.out.print("\nEnter Product ID: ");

        int id=sc.nextInt();

        Product product =
        productService.getProduct(id);

        if(!Validator.validateProduct(product)){

            System.out.println("Invalid Product");
            return;
        }

        // Apply discount
        product.setDiscountStrategy(
        new PercentageDiscount(10));

        Order order=new Order();

        order.addProduct(product);

        // Add observers
        order.addObserver(new EmailNotifier());
        order.addObserver(new SMSNotifier());

        System.out.print(
        "Enter Payment Method (card/paypal): ");

        String payment=sc.next();

        orderService.placeOrder(order,payment);

        orderService.shutdown();
    }
}
