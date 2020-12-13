package jade;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

/**
 * Class of agent that produce an consume products
 */
public class ProducterConsumerAgent extends Agent {

	protected double satisfaction;
	protected Product production;
	protected Product consumption;
	protected int maxStock;
	protected int productionRythm;
	protected int consumptionRythm;
	protected double money;

	/**
	 * 
	 */
	protected void setup() {

		Object[] args = getArguments();
		if ((args != null) && (args.length != 0)) {

			try {

				this.satisfaction = 1;
				String productionArgument = args[0].toString();

				switch (productionArgument) {
				case "A":
					this.production = new ProductA(1, 0);
					break;
				case "B":
					this.production = new ProductB(1, 0);
					break;
				case "C":
					this.production = new ProductC(1, 0);
					break;
				default:
					this.production = new Product(1, 0);
					break;
				}

				String consumptionArgument = args[1].toString();

				switch (consumptionArgument) {
				case "A":
					this.consumption = new ProductA(1, 0);
					break;
				case "B":
					this.consumption = new ProductB(1, 0);
					break;
				case "C":
					this.consumption = new ProductC(1, 0);
					break;
				default:
					this.consumption = new Product(1, 0);
					break;
				}

				this.maxStock = Integer.parseInt(args[2].toString());
				this.productionRythm = Integer.parseInt(args[3].toString());
				this.consumptionRythm = Integer.parseInt(args[4].toString());
				this.money = 50;
				System.out.println(getLocalName() + " " + consumption.getTypeProduct() + " " + maxStock + " "
						+ productionRythm + " " + consumptionRythm);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		addBehaviour(new OneShotBehaviour(this) {
			public void action() {
				ServiceDescription sd = new ServiceDescription();
				sd.setType(getProduction().getTypeProduct());
				sd.setName(getLocalName());
				registerToDF(sd);
				System.out.println(getLocalName() + " registered to DF :\n\t" + toString() + "\n");
			}
		});

		addBehaviour(new TickerBehaviour(this, 100) {

			public void onTick() {

				satisfactionLog(getLocalName(),satisfaction);
				System.out.println("---- "+getLocalName() + "\n#satisfatcion = " + satisfaction 
						+ "\n#production stock = "+ production.getQuantity() +" / "+maxStock
						+ "\n#consomation = " + consumption.getQuantity()  +" / "+consumptionRythm
						+ "\n#money = "+ money 
						+ "\n#price of product = " + production.getPrice() + "�");

				if (getProduction().getQuantity() + productionRythm <= maxStock)
					produceProduct();

				if (consumption.getQuantity() >= consumptionRythm) {
					consumeProduct();
					updateSatisfaction(1.0);
				}

				else {
					updateSatisfaction(satisfaction * 0.9);
					buyProduct();
				}

				if (production.getQuantity() > 0.0)
					sellProduct();

				if (satisfaction < 0.5 && money <= 0) //probleme = pas assez de money veut pas dire money<=0
					updateProductPrice(0.9);

				else if (satisfaction == 1 && money > 0)
					updateProductPrice(1.1);

				block();
			}

		});
	}

	/**
	 * 
	 * @param sd ServiceDescription
	 */
	public void registerToDF(ServiceDescription sd) {
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
	 *
	 * 
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
				System.err.println("                                            "+getLocalName()+"__"+reply.getContent());

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
							System.err.println("                                            "+getLocalName()+"__"+reply.getContent());

							money += (nbRequired * production.getPrice());
							production.decrementeStock(nbRequired);
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
	 * 
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
							System.err.println("                                            "+getLocalName()+"__"+reply.getContent());

							msgT = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
							msg = receive(msgT);

							if (msg != null) {
								if (msg.getContent().contains("CONFIRM")) {
									
									String confirmed = msg.getContent().split(":")[1];
									String[] tabConfirm = confirmed.split(";");
									int nbBought = Integer.parseInt(tabConfirm[0]);
									cost = Double.parseDouble(tabConfirm[1]);
									money -= cost;
									consumption.incrementeStock(nbBought);
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

	
	
	public void satisfactionLog(String agentName,double satisfaction){
		
		 try {
			   File file = new File("logSatisfaction/"+agentName+".txt");

			   if (!file.exists())
			    file.createNewFile();

			   FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			   BufferedWriter bw = new BufferedWriter(fw);
			   bw.write(Double.toString(satisfaction) + System.getProperty("line.separator"));
			   bw.close();

			  } catch (IOException e) {
			   e.printStackTrace();
			  }
	}
	
	public void produceProduct() {
		production.incrementeStock(productionRythm);
	}

	public void consumeProduct() {
		consumption.decrementeStock(consumptionRythm);
	}

	public void updateSatisfaction(double satisfaction) {
		this.satisfaction = satisfaction;
	}

	public void updateProductPrice(double ratePrice) {
		production.updatePrice(ratePrice);
		
		if (!production.isAugmentedPrice()) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent("INFORM: SALES... decrease of prices !");
			send(msg);
			System.err.println("                                            "+getLocalName()+"__"+msg.getContent());
		}			
	}

	public double getSatisfaction() {
		return satisfaction;
	}

	public Product getProduction() {
		return production;
	}

	public Product getConsumption() {
		return consumption;
	}

	public int getMaxStock() {
		return maxStock;
	}

	public int getProductionRythm() {
		return productionRythm;
	}

	public int getConsumptionRythm() {
		return consumptionRythm;
	}

	public double getMoney() {
		return money;
	}

	@Override
	public String toString() {
		return ("" + this.getClass().getSimpleName() + "(satisfaction=" + this.getSatisfaction() + ", production="
				+ this.getProduction().toString() + ", consumption=" + this.getConsumption().toString() + ", maxStock="
				+ this.getMaxStock() + ", productionRate=" + this.getProductionRythm() + ", consumptionRate="
				+ this.getConsumptionRythm() + ", money=" + this.getMoney() + ")");
	}

}