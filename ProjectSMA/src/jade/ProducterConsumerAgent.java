package jade;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

/**
 * This class where we describe functions to handle the production and the
 * consumption of the differents products
 */
public class ProducterConsumerAgent extends Agent {

	/**
	 * Satisfaction of the agent, which varies according to the consumption and
	 * production of our agent
	 */
	protected double satisfaction;

	/**
	 * The current stock of production of our agent
	 */
	protected Product production;

	/**
	 * The current stock of consumption of our agent
	 */
	protected Product consumption;

	/**
	 * The maximum of stock of production of the agent
	 */
	protected int maxStock;

	/**
	 * The rhythm of production of our agent
	 */
	protected int productionRhythm;

	/**
	 * The rhythm of consumption of our agent
	 */
	protected int consumptionRhythm;

	/**
	 * The current money of our agent
	 */
	protected double money;

	
	/**
	 * The function to SETUP our agent with two behaviours : <br>
	 * <br>
	 * 
	 * <b>TickerBehaviour</b> : a behaviour that periodically executes in our case
	 * each (100 ms)<br>
	 * <b>OneShotBehaviour</b> : an atomic behaviour that executes just once
	 */
	protected void setup() {

		cleanLogs();

		Object[] args = getArguments();
		if ((args != null) && (args.length != 0)) {

			try {
				this.satisfaction = 1;
				this.money = 50;
				this.maxStock = Integer.parseInt(args[2].toString());
				this.productionRhythm = Integer.parseInt(args[3].toString());
				this.consumptionRhythm = Integer.parseInt(args[4].toString());

				if (args[0].toString().equals("C"))
					this.production = new ProductC(1, 0);
				else if (args[0].toString().equals("B"))
					this.production = new ProductB(1, 0);
				else
					this.production = new ProductA(1, 0);

				if (args[1].toString().equals("A"))
					this.consumption = new ProductA(1, 0);
				else if (args[1].toString().equals("C"))
					this.consumption = new ProductC(1, 0);
				else
					this.consumption = new ProductB(1, 0);

				System.out.println("Agent " + getLocalName() + " has been created = consumption : "
						+ consumption.getTypeProduct() + " max stock : " + maxStock + " production rythm : "
						+ productionRhythm + " consumption rythm : " + consumptionRhythm);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		addBehaviour(new OneShotBehaviour(this) {
			public void action() {
				ServiceDescription sd = new ServiceDescription();
				sd.setType(getProduction().getTypeProduct());
				sd.setName(getLocalName());
				register(sd);
				System.out.println("Agent " + getLocalName() + " has been registered ");
			}
		});

		addBehaviour(new TickerBehaviour(this, 1000) {

			public void onTick() {

				satisfactionLog(getLocalName(), satisfaction);
				System.out.println("---- " + getLocalName() + "\n#satisfatcion = " + satisfaction
						+ "\n#production stock = " + production.getQuantity() + " / " + maxStock + "\n#consomation = "
						+ consumption.getQuantity() + " / " + consumptionRhythm + "\n#money = " + money
						+ "\n#price of product = " + production.getPrice() + "�");

				if (getProduction().getQuantity() + productionRhythm <= maxStock)
					produceProduct();

				if (consumption.getQuantity() >= consumptionRhythm) {
					consumeProduct();
					updateSatisfaction(1.0);
				}

				else {
					updateSatisfaction(satisfaction * 0.9);
					buyProduct();
				}

				if (production.getQuantity() > 0.0)
					sellProduct();

				if (satisfaction < 0.5 && money <= 0) // pas assez de money veut pas dire money<=0 
					updateProductPrice(0.9);

				else if (satisfaction == 1 && money > 0)
					updateProductPrice(1.1);

				block();
			}

		});
	}

	/**
	 * function that register a service to the DF
	 * 
	 * @param sd : the description of our service
	 */
	public void register(ServiceDescription sd) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	/**
	 * Function that sell a product by the agent and do propositions to other agents
	 * in the system
	 */
	public void sellProduct() {

		MessageTemplate msgT = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = receive(msgT);

		if (msg != null) {
			if (msg.getContent().equals("CFP")) {

				ACLMessage reply = msg.createReply();
				reply.setContent("PROPOSE:" + String.valueOf(production.getQuantity()) + ";"
						+ String.valueOf(production.getPrice()));
				reply.setPerformative(ACLMessage.PROPOSE);
				send(reply);
				System.err.println("                      " + getLocalName() + "__" + reply.getContent());

				msgT = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				msg = receive(msgT);

				if (msg != null) {
					if (msg.getContent().contains("ACCEPT")) {

						String[] accepted = msg.getContent().split(":");
						int nbRequired = (int) Double.parseDouble(accepted[1]);

						if (nbRequired <= production.getQuantity()) {
							reply = msg.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							reply.setContent("CONFIRM:" + String.valueOf(nbRequired) + ";"
									+ String.valueOf(nbRequired * production.getPrice()));
							send(reply);
							System.err.println("                      " + getLocalName() + "__" + reply.getContent());

							money += (nbRequired * production.getPrice());
							production.decrementStock(nbRequired);
						} else {
							reply = msg.createReply();
							reply.setPerformative(ACLMessage.CANCEL);
							reply.setContent("CANCEL");
							send(reply);
						}
					} else if (msg.getContent().contains("REJECT")) {
						reply = msg.createReply();
						reply.setPerformative(ACLMessage.CANCEL);
						reply.setContent("CANCEL");
						send(reply);
					}
				}
			}
		}
	}

	/**
	 * Function to by a certain product from another agent And see products
	 * propositions
	 */
	public void buyProduct() {

		boolean buy = false;
		ServiceDescription sd = new ServiceDescription();
		sd.setType(getConsumption().getTypeProduct());
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);

		try {
			DFAgentDescription[] sellers = DFService.search(this, dfd);
			int index = 0;
			while (index < sellers.length && buy == false) {
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);

				msg.addReceiver(sellers[index].getName());
				msg.setContent("CFP");
				send(msg);

				MessageTemplate msgT = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				msg = receive(msgT);

				if (msg != null) {
					if (msg.getContent().contains("PROPOSE")) {

						String proposed = msg.getContent().split(":")[1];
						String[] tabPropose = proposed.split(";");
						int nbQuantityPossible = Integer.parseInt(tabPropose[0]);
						double cost = Double.parseDouble(tabPropose[1]);

						ACLMessage reply = msg.createReply();

						if (money > cost) {
							if (money > nbQuantityPossible * cost)
								reply.setContent("ACCEPT:" + nbQuantityPossible);
							else
								reply.setContent("ACCEPT:" + money / cost);

							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							send(reply);
							System.err.println("                      " + getLocalName() + "__" + reply.getContent());

							msgT = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
							msg = receive(msgT);

							if (msg != null) {
								if (msg.getContent().contains("CONFIRM")) {

									String confirmed = msg.getContent().split(":")[1];
									String[] tabConfirm = proposed.split(";");
									int nbBought = Integer.parseInt(tabConfirm[0]);
									cost = Double.parseDouble(tabConfirm[1]);
									money -= cost;
									consumption.incrementStock(nbBought);
									buy = true;

								} else if (msg.getContent().contains("CANCEL"))
									index++;
							}
						} else {
							reply.setContent("REJECT");
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							send(reply);
							index++;
						}
					} else
						index++;
				} else
					index++;
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	/**
	 * Function to delete files where we stored satisfactions values needed if the
	 * program run multiple times
	 */
	public void cleanLogs() {
		File folder = new File("logSatisfaction");
		File[] listOfFiles = folder.listFiles();

		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					file.delete();
				}
			}
		} else {
			System.out.println("Directory logs is already cleaned...");
		}
	}

	/**
	 * Function to write the satisfaction value to the file of the agent
	 * 
	 * @param agentName    : name of the agent
	 * @param satisfaction : satisfaction value
	 */
	public void satisfactionLog(String agentName, double satisfaction) {

		try {
			File file = new File("logSatisfaction/" + agentName + ".txt");
			if (!file.exists())
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(Double.toString(satisfaction) + System.getProperty("line.separator"));
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function that produce the product for the agent and that update the current
	 * stock of production
	 */
	public void produceProduct() {
		production.incrementStock(productionRhythm);
	}

	/**
	 * Function that consume the product for the agent and that update the current
	 * stock of consumption
	 */
	public void consumeProduct() {
		consumption.decrementStock(consumptionRhythm);
	}

	/**
	 * Function that update the satisfaction of the agent
	 * 
	 * @param satisfaction : new satisfaction
	 */
	public void updateSatisfaction(double satisfaction) {
		this.satisfaction = satisfaction;
	}

	/**
	 * Function that update the price of product, and if the price decrease we
	 * announce sales to the others agents
	 * 
	 * @param ratePrice : The rate of the update
	 */
	public void updateProductPrice(double ratePrice) {
		production.updatePrice(ratePrice);

		if (!production.isAugmentedPrice()) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent("INFORM: SALES... decrease of prices !");
			send(msg);
			System.err.println("                                   " + getLocalName() + "__" + msg.getContent());
		}
	}

	/**
	 * Getter of the current satisfaction of the agent
	 * 
	 * @return current satisfaction
	 */
	public double getSatisfaction() {
		return satisfaction;
	}

	/**
	 * Getter of the current stock of production of the agent
	 * 
	 * @return current stock of production
	 */
	public Product getProduction() {
		return production;
	}

	/**
	 * Getter of the current stock of consumption of the agent
	 * 
	 * @return current stock of consumption
	 */
	public Product getConsumption() {
		return consumption;
	}

	/**
	 * Getter of the maximum of stock of the agent
	 * 
	 * @return maximum of stock for the agent
	 */
	public int getMaxStock() {
		return maxStock;
	}

	/**
	 * Getter of the rhythm of production of the agent
	 * 
	 * @return rhythm of production
	 */
	public int getProductionRhythm() {
		return productionRhythm;
	}

	/**
	 * Getter of the rhythm of consumption of the agent
	 * 
	 * @return rhythm of consumption
	 */
	public int getConsumptionRhythm() {
		return consumptionRhythm;
	}

	/**
	 * Getter of the current money of the agent
	 * 
	 * @return current money of the agent
	 */
	public double getMoney() {
		return money;
	}

	@Override
	public String toString() {
		return "ProducterConsumerAgent [satisfaction=" + satisfaction + ", production=" + production + ", consumption="
				+ consumption + ", maxStock=" + maxStock + ", productionRythm=" + productionRhythm
				+ ", consumptionRythm=" + consumptionRhythm + ", money=" + money + "]";
	}

}
