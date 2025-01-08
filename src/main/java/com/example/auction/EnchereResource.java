package com.example.auction;


import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Path("/Encheres")
public class EnchereResource {

    @Inject
    EntityManager em;

    @Inject
    private EnchereManager manager;

    @Inject
    private EnchereService es;

    @Inject
    @RestClient
    private PokemonServiceClient pks;
    //on doit develppé une intrface pour le client rest


    @POST
    @Path("/{userid}/{pokemonid}/{amount}")
    public Long createEnchere(@PathParam("userid") Long userid,@PathParam("pokemonid") Long pokemonid,@PathParam("amount") Long amount) {
        return es.createEnchere(userid,pokemonid,amount);
    }

    @POST
    @Path("/creteEnchereAleatoire")
    public void creteEnchereAleatoire() {
        es.createAuctionAutomatically();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/{userId}/{Bid}")
    @Transactional
    public Response placerBid(@PathParam("id") Long id, @PathParam("userId") Long userID, @PathParam("Bid") double bid) {
        Enchere enchere = manager.findEnchere(id);
        String msg = es.placerBid(id, userID, bid);

        if ("ecnhere not found".equals(msg)) {
            return Response.status(Response.Status.NOT_FOUND).entity("enchere not found").build();
        }
        if ("Mise trop faible".equals(msg)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("mise en place trés faible").build();
        }
        return Response.ok(enchere).build();


    }

    @GET
    @Path("/Enchere/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Enchere getEncherebyId(@PathParam("id") Long id) {
        Enchere e = manager.findEnchere(id);

        return e;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Enchere> getAllEncheres() {
        return manager.findActiveAuctions();
    }

    @GET
    @Path("/{type}")
    public List<Enchere> getAllEncheresByType(@PathParam("type") String type) {
        return es.getEnchereParType(type);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnchere(@PathParam("id") Long id) {
        Enchere e = manager.findEnchere(id);
        if (e == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("enchere not found").build();
        }
        return Response.ok(e).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{pokemonId}/addAuctionHistory")
    public Response addAuctionHistory(@PathParam("pokemonId") Long pokemonId, Enchere enchere) {
        try {
            pks.addAuctionHistory(pokemonId, enchere);
            return Response.ok("Enchère ajoutée avec succès").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("une rerur est servenue").build();
        }

    }

    @DELETE
    @Path("/{enchereId}/bids/{userId}")
    @Transactional
    public Response enleverBid(@PathParam("enchereId") Long enchereId, @PathParam("userId") Long userId) {
        try {
            es.EnleverBid(enchereId, userId);
            return Response.ok("Bid removed successfully").build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred while removing the bid").build(); }
    }
}