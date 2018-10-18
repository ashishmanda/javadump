package cmsc433.p1;


/**
 * Class provided for ease of test. This will not be used in the project 
 * evaluation, so feel free to modify it as you like.
 */ 
public class Simulation
{
    public static void main(String[] args)
    {                
        int nrSellers = 50;
        int nrBidders = 20;
        
        Thread[] sellerThreads = new Thread[nrSellers];
        Thread[] bidderThreads = new Thread[nrBidders];
        Seller[] sellers = new Seller[nrSellers];
        Bidder[] bidders = new Bidder[nrBidders];
        
        // Start the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            sellers[i] = new Seller(
            		AuctionServer.getInstance(), 
            		"Seller"+i, 
            		100, 50, i
            );
            sellerThreads[i] = new Thread(sellers[i]);
            System.out.println(sellers[i].name() + " is Thread-" + sellerThreads[i].getId());
            sellerThreads[i].start();
        }
        
        // Start the conservative buyers
        for (int i=0; i<nrBidders/2; ++i)
        {
            bidders[i] = new ConservativeBidder(
            		AuctionServer.getInstance(), 
            		"Conservative Buyer "+i, 
            		100, 20, 150, i
            );
            
            bidderThreads[i] = new Thread(bidders[i]);
            
            System.out.println(bidders[i].name() + " is Thread-" + bidderThreads[i].getId());
            bidderThreads[i].start();
        }
        
     // Start the Agressive buyers
        for (int i=nrBidders/2; i<nrBidders; ++i)
        {
            bidders[i] = new AggressiveBidder(
            		AuctionServer.getInstance(), 
            		"Agressive Buyer "+(i +(nrBidders/2)), 
            		100, 20, 150, i
            );
            bidderThreads[i] = new Thread(bidders[i]);
            System.out.println(bidders[i].name() + " is Thread-" + bidderThreads[i].getId());
            bidderThreads[i].start();
        }
        
        // Join on the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            try
            {
                sellerThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // Join on the bidders
        for (int i=0; i<nrBidders; ++i)
        {
            try
            {
                bidderThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // TODO: Add code as needed to debug
        
    }
}
