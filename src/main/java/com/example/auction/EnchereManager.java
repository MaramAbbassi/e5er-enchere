package com.example.auction;


import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EnchereManager {
    @Inject
    @RestClient
    PokemonServiceClient pokemonServiceClient;
    @Inject
    private EntityManager em;

    public List<Enchere> findActiveAuctions(){
        return em.createQuery("select a from Enchere a where a.Status=:status",Enchere.class).setParameter("status","active").getResultList();
    }

    public Enchere findEnchere(Long id){
        return em.createQuery("select e from Enchere e where e.id=:id",Enchere.class).setParameter("id",id).getSingleResult();

    }

    @Transactional
    public void createEncher(Enchere encher){
        em.persist(encher);
    }

    public int countActiveAuctions(){
        return em.createQuery("select count(a) from Enchere a where a.Status=:status",Long.class).setParameter("status","active").getSingleResult().intValue();

    }

    public List<Enchere> findExpiredEncheres(){
        return em.createQuery("select e from Enchere e where e.Status=:status AND e.dateExpiration<:now" ,Enchere.class).setParameter("status","active").setParameter("now", LocalDateTime.now()).getResultList();
    }

    @Transactional
    public void miseAjourEnchere(Enchere encher){
        em.merge(encher);
    }


    public List<Enchere> getEnchereParType(String string){
        List<Enchere> encheres=findActiveAuctions();
        List<Enchere> enchreretourne=new ArrayList<>();
        for(Enchere e:encheres){
            Pokemon pokemon=pokemonServiceClient.trouverPokemon(e.getPokemonId());
            for(String s: pokemon.getTypes()){
                if(s.equalsIgnoreCase(string)){
                    enchreretourne.add(e);
                }
            }
        }
        return enchreretourne;
    }
//getEnchereByUserId

    //get bid d'un enchere by user id
    public Bid getUserEnchereBid(Long userId,Long encherid){
        Enchere enchere=findEnchere(encherid);
        Bid bid=em.createQuery("select a from Bid a where a.userId=:userid AND a.enchere=:enchere", Bid.class).setParameter("userid",userId).setParameter("enchere",enchere).getSingleResult();
        return bid;
    }
}
