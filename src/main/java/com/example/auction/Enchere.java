package com.example.auction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
public class Enchere  {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    private Long pokemonId;
    private double startingPrice; //prix de debut d'enchére
    private double highestBid; //enchere le plus haut
    private Long highestBidderId;
    private LocalDateTime dateExpiration ;//date d'expiration
    private String Status; //cloturé actif..

    @OneToMany(mappedBy = "enchere", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @JsonManagedReference//indique que cette relation et le parent
    private List<Bid> bids;

    public Enchere() {
    }

    public Enchere(Long userid, long pokenmonid, double startingPrice, double highestBid, Long highestBidderId, LocalDateTime dateExpiration, String Status) {

        this.pokemonId = pokenmonid;
        this.startingPrice = startingPrice;
        this.highestBid = highestBid;
        this.highestBidderId = highestBidderId;
        this.dateExpiration = dateExpiration;
        this.Status = Status;
    }
    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return highestBidderId;
    }

    public void setUserId(Long userId) {
        this.highestBidderId = userId;
    }

    public Long getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(Long pokemonId) {
        this.pokemonId = pokemonId;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public Long getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(Long highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }
}
