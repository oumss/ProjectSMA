 Projet réalisé par : 
 
 MOUTAWADII Oumaima
 DAOUD Sariah
 
 									(2020 Module SMA)
 									(Philipe Laroque)
 							
 /***************************************************************/
 ****************************************************************
 /**************************************************************/
 
 ### Pour lancer notre projet : 
 
 #	ETAPE 1 
 Compiler le projet avec "javac"  
 
 #	ETAPE 2 
 Executer le projet a l'aide de la commande ci-dessous (Celle-ci créera trois agents qui se completent les uns aux autres.)
 java jade.Boot -gui A:jade.ProducterConsumerAgent(A,B,20,5,5);B:jade.ProducterConsumerAgent(B,C,10,5,4);C:jade.ProducterConsumerAgent(C,A,30,5,6)
 
 # ETAPE 3
 Observer le comportement des agents...
 
 /***************************************************************/
 ****************************************************************
 /**************************************************************/
 
 Les arguments sont les suivants : 
 1. Le type de produit que l'agent produit
 2. Le type de produit que l'agent consome
 3. le maximum de stock de production
 4. le rythme de production  de produit
 5. le rythme de consomation de produit
 
 
 
 
 