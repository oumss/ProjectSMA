package jade;

/**
 * Class representing a product (produced or consumed by an agent)
 */
public class Product { 
    protected float price; 
    protected float quantity; 

    public Product(float price, float quantity) 
    { 
        this.price = price; 
        this.quantity = quantity; 
    } 
  
    public void addToStock(float quantity) { this.quantity += quantity; } 
    public void removeFromStock(float quantity) { this.quantity -= quantity; }

    public void updatePrice(float rate) { this.price *= rate; }
    public float getPrice() { return this.price; }
    public float getQuantity() { return this.quantity; }
    public String getType() { return this.getClass().getSimpleName(); }
  
    @Override
    public String toString() 
    { 
        return ("" + this.getClass().getSimpleName() + "(price=" + this.price + ", quantity=" + this.quantity + ")"); 
    } 
  
    public static void main(String[] args) 
    { 
        Product product = new Product(4, 3); 
        System.out.println(product.toString()); 
    } 
} 
