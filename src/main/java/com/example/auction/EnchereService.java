package com.example.auction;




import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class EnchereService {

    private static final Logger log = LoggerFactory.getLogger(EnchereService.class);

    @Inject
    private EnchereManager em;

    @Inject
    private EntityManager em2;
    @Inject
    @RestClient
    PokemonServiceClient pokemonService;

    @Inject
    private EnchereManager enem;

    @Inject
    @RestClient
    private UserServiceClient userservice;

    private Random random=new java.util.Random();



    @Transactional
    public void createAuctionAutomatically() {
        Pokemon pokemon=pokemonService.pokemonAleatoire();
        double baseprice=pokemon.getValeurReelle();

        Enchere enchere=new Enchere();
        enchere.setPokemonId(pokemon.getId());
        enchere.setStartingPrice(baseprice);
        enchere.setHighestBid(0);
        enchere.setHighestBidderId(null);
        enchere.setDateExpiration(LocalDateTime.now().plusHours(24));
        enchere.setStatus("active");
        em.createEncher(enchere);


    }

    @Transactional
    public void closeExpiredEnchere() {
        List<Enchere> expiredEncheres = enem.findExpiredEncheres();

        for (Enchere e : expiredEncheres) {
            if (e.getHighestBid() != 0 && e.getHighestBidderId() != null) {
                boolean paymentSuccessful = userservice.deductLimCoins(e.getHighestBidderId(),(int) e.getHighestBid());

                if (paymentSuccessful) {
                    // Transférer le Pokémon au gagnant
                    userservice.addPokemonToUser(e.getHighestBidderId(), pokemonService.trouverPokemon(e.getPokemonId()));
                    // Notifier le service Pokémon
                    //envoi du mail au user
                    // pokemonService.addAuctionHistory(e.getPokemonId(), e);
                } else {
                    // Passer au second meilleur enchérisseur
                    handleNextHighestBidder(e);
                }
            }
            e.setStatus("closed");
            enem.miseAjourEnchere(e);
        }
    }

    private void handleNextHighestBidder(Enchere enchere) {
        List<Bid> allBids = enchere.getBids(); // Récupère toutes les enchères associées
        allBids.sort((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount())); // Trier par montant décroissant

        for (int i = 1; i < allBids.size(); i++) { // Commence par le second enchérisseur
            Bid nextBid = allBids.get(i);
            boolean paymentSuccessful = userservice.deductLimCoins(nextBid.getUserId(),(int) nextBid.getAmount());

            if (paymentSuccessful) {
                userservice.addPokemonToUser(nextBid.getUserId(), pokemonService.trouverPokemon(enchere.getPokemonId()));
                break;
            }
        }
    }



    @Transactional
    public String placerBid(Long enchereId,Long userId,double amount){
        Enchere enchere=enem.findEnchere(enchereId);
        if(enchere==null || !"active".equals(enchere.getStatus())){
            return "enchere not found!";
        }
        if(amount<enchere.getHighestBid()){
            return "Mise trop faible";
        }
        Bid bid=new Bid(enchere,userId,amount,LocalDateTime.now());
        em2.persist(bid);
        enchere.setHighestBid(amount);
        enchere.setHighestBidderId(userId);
        enchere.getBids().add(bid);
        em.miseAjourEnchere(enchere);
        //fonction bech tzid fel tableau ta3 lista active encheres
        //userservice.addEnchereToActive(userId,enchereId);
        pokemonService.addAuctionHistory(enchere.getPokemonId(), enchere);
        return "enchere mis";
    }
    //def reource w restclient user
    /*@Transactional
    public void EnleverBid(Long enchereID,Long userId){
        Enchere enchere=findEnchereById(enchereID);
        Bid bid=em.getUserEnchereBid(enchereID,userId);
        enchere.getBids().remove(bid);
        // If this bid was the highest, reset the auction's highest bid
        if (enchere.getHighestBidderId().equals(userId)) {
            // Update to the next highest bid
            enchere.getBids().stream()
                    .max((b1, b2) -> Double.compare(b1.getAmount(), b2.getAmount()))
                    .ifPresent(nextHighestBid -> {
                        enchere.setHighestBid(nextHighestBid.getAmount());
                        enchere.setHighestBidderId(nextHighestBid.getUserId());
                        em.miseAjourEnchere(enchere);
                    });
        }
    }

     */
    @Transactional
    public void EnleverBid(Long enchereID, Long userId) {
        // Find the auction by its ID
        Enchere enchere = findEnchereById(enchereID);
        if (enchere == null) {
            throw new IllegalArgumentException("Auction with ID " + enchereID + " not found.");
        }

        // Find the bid for the given user and auction
        Bid bidToRemove = enchere.getBids().stream()
                .filter(bid -> bid.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No bid found for user " + userId + " in auction " + enchereID));

        // Remove the bid
        enchere.getBids().remove(bidToRemove);
        em2.remove(bidToRemove);

        // Check if the bid being removed is the highest bid
        if (enchere.getHighestBidderId() != null && enchere.getHighestBidderId().equals(userId)) {
            // Reset the highest bid and bidder to the next highest bid
            enchere.getBids().stream()
                    .max((b1, b2) -> Double.compare(b1.getAmount(), b2.getAmount()))
                    .ifPresentOrElse(
                            nextHighestBid -> {
                                enchere.setHighestBid(nextHighestBid.getAmount());
                                enchere.setHighestBidderId(nextHighestBid.getUserId());
                            },
                            () -> {
                                // If no bids are left, reset the highest bid and bidder
                                enchere.setHighestBid(0);
                                enchere.setHighestBidderId(null);
                            }
                    );

            // Update the auction in the database
            em2.merge(enchere);
        }
    }


    //chercher enchere par type de pokemon

    public List<Enchere> getEnchereParType(String type){
        return enem.getEnchereParType(type);
    }
    public Enchere findEnchereById(Long enchereID){
        return enem.findEnchere(enchereID);
    }
     //fonction tasna3 enchere ki yotleb el user

 public Long createEnchere(Long  userid,Long pokemonid,double amount) {
        Enchere enchere=new Enchere();
        enchere.setDateExpiration(LocalDateTime.now().plusHours(24));
        enchere.setStatus("active");
        enchere.setHighestBidderId(null);
        enchere.setHighestBid(0);
        enchere.setPokemonId(pokemonid);
        enchere.setUserId(userid);
        return enchere.getId();
 }
}
