package com.tanish.lld.observer.AuctionSystem;

import java.math.BigDecimal;
import java.util.*;

//AUCTIONEER & BIDDER
class User {

    private final String userId;
    private final String name;
    private final String email;
    private final String phone;

    public User(
            String userId,
            String name,
            String email,
            String phone) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}

class Bid{
    private final User bidder;
    private final BigDecimal amount;
    private final long timestamp;

    Bid(User bidder, BigDecimal amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }


    public User getBidder() {
        return bidder;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


//OBSERVER PATTERN
//OBSERVER
interface AuctionObserver{
    void updateOnBidPlaced(Auction auction,Bid newBid);
}
class Bidder implements AuctionObserver{

    private final User bidder;

    Bidder(User bidder) {
        this.bidder = bidder;
    }

    @Override
    public void updateOnBidPlaced(Auction auction, Bid newBid) {

        if(newBid.getBidder().getUserId().equals(bidder.getUserId())){
            return;
        }
        System.out.println( "[NOTIFICATION] " + newBid.getBidder().getName() + " -> New highest bid of " + newBid.getAmount() + " on auction " + auction.getAuctionId() );
    }
}

//SUBJECT
interface AuctionSubject {
    void subscribe(AuctionObserver observer);
    void unsubscribe(AuctionObserver observer);
    void notifyObservers(Bid bid); }
class Auction implements AuctionSubject{
    private final String auctionId;
    private final String item;
    private final BigDecimal startingPrice;
    private BigDecimal highestPrice;
    private Bid highestBid;

    private boolean active=true;
    private final Set<AuctionObserver> observers = new HashSet<>();

    Auction(String auctionId, String item, BigDecimal startingPrice) {
        this.auctionId = auctionId;
        this.item = item;
        this.startingPrice = startingPrice;
        highestPrice=startingPrice;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItem() {
        return item;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getHighestPrice() {
        return highestPrice;
    }

    public Bid getHighestBid() {
        return highestBid;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void subscribe(AuctionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(AuctionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Bid bid) {
        for (AuctionObserver observer:observers){
            observer.updateOnBidPlaced(this, bid);
        }
    }
    public void placeBid(User bidder, BigDecimal amount){
        if(!active){
            throw new IllegalArgumentException("Auction is no longer active...");
        }

        if(amount.compareTo(highestPrice) <= 0){
            throw new IllegalArgumentException("Bid must be greater than current highest bid");
        }

        Bid bid=new Bid(bidder, amount);
        highestPrice=amount;
        highestBid=bid;
        System.out.println( "New highest bid: " + bidder.getName() + " -> " + amount );
        notifyObservers(bid);

    }
    public void closeAuction(){
        active=false;
        if (highestBid == null){
            System.out.println("Auction closed without any bids...");
        }else {
            System.out.println( "Auction won by " + highestBid.getBidder().getName() + " for " + highestBid.getAmount() );
        }
    }
}

//FACADE/SERVICE
class AuctionService{
    private final Map<String, Auction> auctions = new HashMap<>();
    public String createAuction(String item, BigDecimal startingPrice){
        String auctionId=UUID.randomUUID().toString();
        Auction auction=new Auction(auctionId, item,startingPrice);
        auctions.put(auctionId,auction);
        return auctionId;

    }
    public void subscribe(String auctionId, Bidder bidder){
        Auction auction=auctions.get(auctionId);
        auction.subscribe(bidder);
    }
    public void unsubscribe(String auctionId, Bidder bidder){
        Auction auction=auctions.get(auctionId);
        auction.unsubscribe(bidder);
    }
    public void placeBid(String auctionId, User bidder, BigDecimal amount){
        Auction auction=auctions.get(auctionId);
        auction.placeBid(bidder,amount);
    }
    public void closeAuction(String auctionId){
        Auction auction=auctions.get(auctionId);
        auction.closeAuction();
    }
    private Auction getAuction(String auctionId){
        Auction auction= auctions.get(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException( "Auction does not exist: " + auctionId );
        }
        return auction;
    }
}
public class AuctionSystemDriver {
    public static void main(String[] args) {
        AuctionService service=new AuctionService();
        User aliceUser = new User("U1", "Alice","alice@example.com","+91-9000000001");
        User bobUser = new User("U2", "Bob","bob@example.com","+91-9000000002");
        User charlieUser = new User("U3", "Charlie", "charlie@example.com","+91-9000000003");
        Bidder alice = new Bidder(aliceUser);
        Bidder bob = new Bidder(bobUser);
        Bidder charlie = new Bidder(charlieUser);

        //create auction
        String auctionId=service.createAuction("MacBook Pro",new BigDecimal(50000));
        //subscriber bidders
        service.subscribe(auctionId,alice);
        service.subscribe(auctionId,bob);
        service.subscribe(auctionId,charlie);

        System.out.println( "\n--------- BIDDING ---------" );
        //alice bids
        service.placeBid(auctionId,aliceUser,new BigDecimal(55000));
        //bob bids
        service.placeBid(auctionId,bobUser,new BigDecimal(60000));
        //charlie bids
        service.placeBid(auctionId, charlieUser, new BigDecimal(65000));

        //bob stops watching the auction
        service.unsubscribe(auctionId, bob);

        System.out.println( "\n--------- AFTER BOB UNSUBSCRIBES ---------" );
        // Alice bids again
        service.placeBid( auctionId, aliceUser, new BigDecimal("70000") );
        // Close auction
        System.out.println( "\n--------- CLOSE AUCTION ---------" );
        service.closeAuction(auctionId);
    }


}
