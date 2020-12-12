package jade;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

/**
 * Class describing the attributes and behaviours of our agent, producting and consuming two different production at given rates, and trying to maximize a level of satisfaction
 */
public class MyAgent extends Agent
{
    /**
     * Level of satisfaction of the agent
     */
    protected float satisfaction;

    /**
     * Product produced by the agent
     */
    protected Product production;

    /**
     * Product consumed by the agent
     */
    protected Product consumption;

    /**
     * Maximum stock of production that the agent can handle
     */
    protected int maxStock;

    /**
     * Rate at which the production quantity increases
     */
    protected int productionRate;

    /**
     * Rate at which the consumption quantity decreases
     */
    protected int consumptionRate;

    /**
     * Amount of money held by the agent
     */
    protected float money;
    
    /**
     * setup of the agent: initialization of its attributes and behaviours
     */
    protected void setup()
    {
        Object[] args = getArguments();
        if ((args != null) && (args.length != 0))
        {
            try {
                this.satisfaction = 1.0f;
                String flagProduction = args[0].toString();
                switch(flagProduction) {
                    case "A": this.production = new ProductA(1.0f, 0.0f); break;
                    case "B": this.production = new ProductB(1.0f, 0.0f); break;
                    case "C": this.production = new ProductC(1.0f, 0.0f); break;
                    default: this.production = new Product(1.0f, 0.0f); break;
                }
                String flagConsumption = args[1].toString();
                switch(flagConsumption) {
                    case "A": this.consumption = new ProductA(1.0f, 0.0f); break;
                    case "B": this.consumption = new ProductB(1.0f, 0.0f); break;
                    case "C": this.consumption = new ProductC(1.0f, 0.0f); break;
                    default: this.consumption = new Product(1.0f, 0.0f); break;
                }
                this.maxStock = Integer.parseInt(args[2].toString()); 
                this.productionRate = Integer.parseInt(args[3].toString()); 
                this.consumptionRate = Integer.parseInt(args[4].toString()); 
                this.money = 10.0f;
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }

        /**
         * OneShotBehaviour used to register the agent to the DF
         * @param  agent agent associated to this behaviour
         * @return null
         */
        addBehaviour(new OneShotBehaviour(this){
            public void action(){ 
                ServiceDescription sd = new ServiceDescription();
                sd.setType(getProduction().getType());
                sd.setName(getLocalName());
                register(sd);
                System.out.println(getLocalName() + " registered to DF :\n\t" + toString() + "\n");
            }
	    });

        /**
         * TickerBehaviour used to update satisfaction of the agent, launch buying or selling actions, launch production and consumption, update production prices
         * @param  agent agent associated to this behaviour
         * @param  period period of time between two updates
         * @return null
         */
        addBehaviour(new TickerBehaviour(this, 1000) {
            /**
             * onTick callback method called everytime the period of wait of the behaviour is over
             */
            public void onTick(){
                System.out.println(getLocalName() + ": " + satisfaction + " | prod=" + production.getQuantity() + " | cons=" + consumption.getQuantity());

                // Produce (if not working increase satisfaction)
                if (getProduction().getQuantity() < maxStock)
                    produce();
                else
                    updateSatisfaction(1.0f);

                // Consume (decrease satisfaction if stock is null)
                if (consumption.getQuantity() > 0)
                    consume();
                else {
                    // Can't consume
                    updateSatisfaction(satisfaction * 0.9f);
                    // Look for consumption
                    buy();
                }

                // If production exists, sell it
                if (production.getQuantity() > 0.0f) {
                    sell();
                }

                // Update price of production if needed
                if (satisfaction < 1 && money == 0)
                    updateProductPrice(0.9f);
                
                else if (satisfaction == 1 && money > 0)
                    updateProductPrice(1.1f);
                
            }
        });
    }

    /**
     * register function used to register the agent and its provided service to the DF
     * @param sd description of the service provided by the agent
     */
    public void register(ServiceDescription sd) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (FIPAException fe) { fe.printStackTrace(); }
    }

    /**
     * produce increases the quantity of the production
     */
    public void produce() {
            production.addToStock(productionRate);
    }

    /**
     * consume decreases the quantity of the consumption
     */
    public void consume() {
            production.removeFromStock(consumptionRate);
    }

    /**
     * updateSatisfaction passes on a new value to the satisfaction attribute of the agent
     * @param satisfaction new satisfaction value
     */
    public void updateSatisfaction(float satisfaction) {
        this.satisfaction = satisfaction;
    }

    /**
     * updateProductPrice modifies the price of the production according to a given rate
     * @param rate value used to modifiy the price of the production
     */
    public void updateProductPrice(float rate) {
        production.updatePrice(rate);
    }

    /**
     * getSatisfaction basic getter
     * @return satisfaction of the agent
     */
    public float getSatisfaction() { return satisfaction; }

    /**
     * getProduction basic getter
     * @return product produced by the agent
     */
    public Product getProduction() { return production; }

    /**
     * getConsumption basic getter
     * @return product consumed by the agent
     */
    public Product getConsumption() { return consumption; }

    /**
     * getMaxStock basic getter
     * @return maximum production quantity of the agent
     */
    public int getMaxStock() { return maxStock; }

    /**
     * getProductionRate basic getter
     * @return rate of production of the agent
     */
    public int getProductionRate() { return productionRate; }

    /**
     * getConsumptionRate basic getter
     * @return rate of consumption of the agent
     */
    public int getConsumptionRate() { return consumptionRate; }

    /**
     * getMoney basic getter
     * @return money amount of the agent
     */
    public float getMoney() { return money; }

    /**
     * toString descripts the state of the agent
     * @return description of the agent's attributes
     */
    @Override
    public String toString() 
    { 
        return("" + this.getClass().getSimpleName() + 
                "(satisfaction=" + this.getSatisfaction() + 
                ", production=" + this.getProduction().toString() + 
                ", consumption=" + this.getConsumption().toString() + 
                ", maxStock=" + this.getMaxStock() +
                ", productionRate=" + this.getProductionRate() +
                ", consumptionRate=" + this.getConsumptionRate() +
                ", money=" + this.getMoney() + 
                ")"); 
    } 

    /**
     * sell handles product request from customers to sell production
     */
    public void sell() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = receive(mt);
        if (msg != null) {
            if (msg.getContent().equals("CFP")) {
                ACLMessage reply = msg.createReply();

                // Generate offer with available quantity and price (quantity;price)                
                reply.setContent("PROPOSE:" + String.valueOf(production.getQuantity()) + ";" + String.valueOf(production.getPrice()));
                send(reply);

                mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                msg = receive(mt);
                if (msg != null) {
                    if (msg.getContent().contains("ACCEPT")) {
                        // Extract required quantity from acceptation
                        String[] offer = msg.getContent().split(":", 0);
                        Float requiredQuantity = Float.parseFloat(offer[1]);
                        if (requiredQuantity <= production.getQuantity()) {
                            reply = msg.createReply();
                            reply.setContent("CONFIRM:" + String.valueOf(requiredQuantity) + ";" + String.valueOf(requiredQuantity * production.getPrice()));
                            send(reply);

                            money += (requiredQuantity * production.getPrice());
                            production.removeFromStock(requiredQuantity);
                        }
                        else {
                            reply = msg.createReply();
                            reply.setContent("CANCEL");
                            send(reply);
                        }
                    }
                    else if (msg.getContent().contains("REJECT")) {
                        reply = msg.createReply();
                        reply.setContent("CANCEL");
                        send(reply);
                    }
                }
            }
        }
    }

    /**
     * buy looks for sellers to buy consumption
     */
    public void buy() {
        boolean bought = false;
        ServiceDescription sd = new ServiceDescription();
        sd.setType(getConsumption().getType());
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            int seller = 0;
            while (seller < result.length && bought == false) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

                msg.addReceiver(result[seller].getName());
                msg.setContent("CFP");
                send(msg);

                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                msg = receive(mt);

                if (msg != null) {
                    if (msg.getContent().contains("PROPOSE")) {
                        String offer = msg.getContent().split(":", 0)[1];
                        Float availableQuantity = Float.parseFloat(offer.split(";", 0)[0]);
                        Float price = Float.parseFloat(offer.split(";", 0)[1]);

                        ACLMessage reply = msg.createReply();

                        if (money > price) {
                            if (money > availableQuantity * price)
                                reply.setContent("ACCEPT:" + availableQuantity);
                            else
                                reply.setContent("ACCEPT:" + money/price);

                            send(reply);

                            mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                            msg = receive(mt);

                            if (msg != null) {
                                if (msg.getContent().contains("CONFIRM")) {
                                    String deal = msg.getContent().split(":", 0)[1];
                                    Float boughtQuantity = Float.parseFloat(offer.split(";", 0)[0]);
                                    price = Float.parseFloat(offer.split(";", 0)[1]);

                                    money -= price;
                                    consumption.addToStock(boughtQuantity);
                                    bought = true;
                                }
                                else if (msg.getContent().contains("CANCEL"))
                                    seller++;
                            }
                        }
                        else {
                            reply.setContent("REJECT");
                            send(reply);
                            seller++;
                        }
                    }else seller++;
                }else seller++;
            }
        } catch (FIPAException fe) { fe.printStackTrace(); }
    }
}