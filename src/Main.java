import java.time.LocalDate;
import java.util.*;

interface Shippable {
    String getName();
    double getWeight();
}

abstract class Product {

    String name;
    double price;
    int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }
}

class ShippableProduct extends Product implements Shippable {
    double weight;

    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}


class ExpirableProduct extends Product {
    LocalDate expireDate;

    public ExpirableProduct(String name, double price, int quantity, LocalDate expireDate) {
        super(name, price, quantity);
        this.expireDate = expireDate;
    }

    public boolean isExpired() {
        return expireDate.isBefore(LocalDate.now());
    }


}


class ExpirableShippableProduct extends ExpirableProduct implements Shippable {
    double weight;

    public ExpirableShippableProduct(String name, double price, int quantity, LocalDate expireDate, double weight) {
        super(name, price, quantity, expireDate);
        this.weight = weight;
    }


    @Override
    public double getWeight() {
        return weight;
    }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}

class Cart {

    int shippables_items = 0; // count the shippable items .

    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) throws Exception {
        if (quantity > product.quantity)
            throw new Exception("Quantity exceeds stock.");


        //here could be case , that he enter 1st time valid quantity and 2nd time valid but to same product
        // so when you add them it will exceed , but we will handle it in the checkout .


        if(product instanceof Shippable)
            shippables_items+=quantity;

        // if it's already exist before
        for (CartItem item : items) {
            if (item.product == product) {
                item.quantity += quantity;
                return;
            }
        }

        items.add(new CartItem(product, quantity));
    }


    public List<ShippableItem> getShippables() {
        List<ShippableItem> list = new ArrayList<>();
        for (CartItem item : items) {
            if (item.product instanceof Shippable) {

                    Shippable p = (Shippable) item.product;
                    list.add(new ShippableItem(p.getName(), p.getWeight(),item.quantity));

            }
        }
        return list;
    }



    // THROW ERROR
    public boolean isEmpty() {
        return items.isEmpty();
    }


    public double getSubtotal() {
        double sum = 0;
        for (CartItem item : items)
            sum += (item.product.price * item.quantity);
        return sum;
    }

    // THROW ERROR
    public boolean hasExpiredItems() {
        for (CartItem item : items) {
            if(item.product instanceof ExpirableProduct) {
                ExpirableProduct exp = (ExpirableProduct)item.product;
                if (exp.isExpired())
                    return true;
            }
        }
        return false;
    }

    // THROW ERROR
    public boolean hasOutOfStockItems() {
        for (CartItem item : items) {
            if (item.quantity > item.product.quantity) return true;
        }
        return false;
    }

    public int shipping_fees(){

        //10$ on each item for ex .
        return shippables_items * 10;
    }
    public void decrementStock() {
        for (CartItem item : items) {
            item.product.quantity -= item.quantity;
        }
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return items;
    }

}



class Customer{
    String name;
    double balance;

    public Customer(String name, double balance) {
        this.name = name; this.balance = balance;
    }


    Cart cart = new Cart();

    public void addToCart(Product product, int quantity) throws Exception {
        cart.add(product, quantity);
    }



    public void checkout() throws Exception {

        if (cart.isEmpty()) throw new Exception("Cart is empty.");
        if (cart.hasOutOfStockItems()) throw new Exception("Out of stock item in cart.");
        if (cart.hasExpiredItems()) throw new Exception("Cart contains expired items.");

        double subtotal = cart.getSubtotal();

        double shippingfees = cart.shipping_fees();

        double total = subtotal + shippingfees;

        if (balance < total) throw new Exception("Insufficient balance.");

        // print shimpmenr
        List<ShippableItem> shippables = cart.getShippables();
        if (!shippables.isEmpty()) ShippingService.ship(shippables);

// print checkout
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems())
            System.out.println(item.quantity + "x " + item.product.name + " " + (item.product.price * item.quantity));
        System.out.println("----------------------");
        System.out.println("Subtotal " + subtotal);
        System.out.println("Shipping " + shippingfees );
        System.out.println("Amount " + total);

        balance -= total;
        System.out.println("Your current balance: " + balance);

        // last thing .
        cart.decrementStock();
        cart.clear();
    }
}

class ShippableItem {
    String name;
    double weight;
    int quantity;

    public ShippableItem(String name, double weight,int quantity) {
        this.name = name;
        this.weight = weight;
        this.quantity=quantity;
    }
}



class ShippingService {
    public static void ship(List<ShippableItem> items) {
        System.out.println("** Shipment notice **");


        double totalWeight = 0;

        for (ShippableItem item : items) {

            System.out.println(item.quantity + "x " + item.name+" "+item.weight+"kg");
            totalWeight += (item.weight*item.quantity);
        }

        System.out.printf("Total package weight %.1fkg%n", totalWeight);
    }



}






public class Main {
    public static void main(String[] args) throws Exception {
        Customer customer = new Customer("Amr", 1000);

        Product cheese = new ExpirableShippableProduct("Cheese ", 100, 10, LocalDate.now().plusDays(3), 0.4);
        Product biscuits = new ExpirableShippableProduct("Biscuits ", 150, 5, LocalDate.now().plusDays(2), 0.7);
        Product tv = new ShippableProduct("tv", 50, 20,2) ;

        customer.addToCart(cheese, 2);
        customer.addToCart(biscuits, 1);
        customer.addToCart(tv, 1);

        customer.checkout();
    }
}