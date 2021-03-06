package ch.uzh.ifi.seal.soprafs16.service;

import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Loot;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexanderhofmann on 25/03/16.
 */
@Service("lootService")
public class LootService {

    private static final Logger logger = LoggerFactory.getLogger(LootService.class);

    @Autowired
    private GameRepository gameRepo;

//    @Autowired
//    private LootRepository lootRepo;

    @Autowired
    private PlayerService playerService;

//    public List<Loot> saveLootsOfAGame(List<Loot> loots) {
//        List<Loot> savedLoots = new ArrayList<>();
//        for (Loot loot : loots) {
//            savedLoots.add(lootRepo.save(loot));
//        }
//        return savedLoots;
//    }

    public List<Loot> listLootsForGame(Long gameId) {
        logger.debug("listLootsForGame");

        List<Loot> result = new ArrayList<>();
        Game game = gameRepo.findOne(gameId);

        if (game != null) {
            result.addAll(game.getLoots());
        } else {
            logger.error("No game found");
        }

        return result;
    }

}
