package jade;

/**
 * Product class
 */
public class Product {

	/**
	 * The price of the product
	 */
	protected double price;

	/**
	 * The quantity of the product
	 */
	protected int quantity;

	/**
	 * True if the price has been augmented, false if the price has been diminued
	 */
	protected boolean augmentedPrice = false;

	/**
	 * Constructor of the product
	 * 
	 * @param price    : the price of the product
	 * @param quantity : the quantity of stock
	 */
	public Product(double price, int quantity) {

		this.price = price;
		this.quantity = quantity;
	}

	/**
	 * Function that increment the stock after a production
	 * 
	 * @param quantity : the quantity of the product
	 */
	public void incrementStock(int quantity) {
		this.quantity += quantity;
	}

	/**
	 * Function that decrement the stock after a selling
	 * 
	 * @param quantity : the quantity of the product
	 */
	public void decrementStock(int quantity) {
		this.quantity -= quantity;
	}

	/**
	 * Function that update the price of the product, and set the boolean
	 * augmentedPrice to true or false.
	 * 
	 * @param rate : the rate which will update the price
	 */
	public void updatePrice(double rate) {
		if (price < price * rate)
			augmentedPrice = true;
		else
			augmentedPrice = false;

		this.price *= rate;
	}

	/**
	 * Getter of the price of the product
	 * 
	 * @return the price of the product
	 */
	public double getPrice() {
		return this.price;
	}

	/**
	 * Getter of quantity of the product in stock
	 * 
	 * @return the quantity in stock
	 */
	public int getQuantity() {
		return this.quantity;
	}

	/**
	 * Function that says if the price has increased or not
	 * 
	 * @return
	 */
	public boolean isAugmentedPrice() {
		return augmentedPrice;
	}

	/**
	 * Funciton that return the type of the product (A, B or C)
	 * 
	 * @return the type of the product
	 */
	public String getTypeProduct() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return ("" + this.getClass().getSimpleName() + "(price=" + this.price + ", quantity=" + this.quantity + ")");
	}

}
