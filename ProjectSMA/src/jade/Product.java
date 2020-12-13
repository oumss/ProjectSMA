package jade;

/**
 * Product class 	
 */
public class Product {
	
	/**
        * @param price : the price of the product 
	* @param quantity :  the quantity of the product 
        */
	protected double price;
	protected int quantity;

	protected boolean augmentedPrice = false;

	public Product(double price, int quantity) {

		this.price = price;
		this.quantity = quantity;
	}
	
	/**
        * @param  quantity : the quantity of the product 
        */

	public void incrementeStock(int quantity) {
		this.quantity += quantity;
	}
	
	public void decrementeStock(int quantity) {
		this.quantity -= quantity;
	}

	/**
        * @param  rate : the rate wich will update the price 
        */
	public void updatePrice(double rate) {
		if(price<price*rate)
			augmentedPrice = true;
		else 
			augmentedPrice = false;
		
		this.price *= rate;
	}

	public double getPrice() {
		return this.price;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public boolean isAugmentedPrice() {
		return augmentedPrice;
	}

	public String getTypeProduct() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return ("" + this.getClass().getSimpleName() + "(price=" + this.price + ", quantity=" + this.quantity + ")");
	}

}
