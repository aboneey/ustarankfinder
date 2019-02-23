package com.usta.service;

import java.util.Map;

import org.hibernate.Session;

import com.usta.model.PlayerRank;
import com.usta.model.RankGenDetails;
import com.usta.util.HibernateUtil;

public class PlayerRankService {

    public void savePlayerRanks(Map<String, Integer> playerRanks, int version) {

        // opens a new session from the session factory
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        long time = System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(time);

        for (String key : playerRanks.keySet()) {
            PlayerRank pRank = new PlayerRank();
            pRank.setName(key);
            pRank.setPlayerRank(playerRanks.get(key).intValue());
            pRank.setGeneratedDate(date);
            pRank.setVersion(version);
            session.persist(pRank);
        }

        RankGenDetails rgd = new RankGenDetails();
        rgd.setGeneratedDate(date);
        rgd.setAgeBracket(version);
        session.persist(rgd);

        // commits the transaction and closes the session
        session.getTransaction().commit();
        session.close();
    }

}
