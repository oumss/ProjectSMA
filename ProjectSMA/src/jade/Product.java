package jade;

/**
 * Class of product
 */
public class Product {
	/**
	 * price of the product
	 */
	protected double price;
	/**
	 * quantity in stock
	 */
	protected int quantity;
	/**
	 * <b>True<b> : if the price has been augmented
	 * <b>False<b> : if the price has been diminued
	 */
	protected boolean augmentedPrice = false;

	public Product(double price, int quantity) {

		this.price = price;
		this.quantity = quantity;
	}

	public void incrementeStock(int quantity) {
		this.quantity += quantity;
	}

	public void decrementeStock(int quantity) {
		this.quantity -= quantity;
	}

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
