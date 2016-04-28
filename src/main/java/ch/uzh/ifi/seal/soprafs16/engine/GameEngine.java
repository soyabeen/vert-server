package ch.uzh.ifi.seal.soprafs16.engine;

import ch.uzh.ifi.seal.soprafs16.constant.CardType;
import ch.uzh.ifi.seal.soprafs16.engine.rule.RuleSet;
import ch.uzh.ifi.seal.soprafs16.model.Loot;
import ch.uzh.ifi.seal.soprafs16.model.Player;
import ch.uzh.ifi.seal.soprafs16.model.Positionable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

/**
 * This engine overs a simple way to process the action phase.
 * The engine handles the game rules/constraints and their correct execution.
 * <p>
 * Created by soyabeen on 20.04.16.
 */
public class GameEngine {

    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);

    private HashMap<CardType, RuleSet> mappingStore;

    public GameEngine() {
        mappingStore = new HashMap<>();
    }

    /**
     * Returns the rule set for the given card. If the needed rule set is not in the store, a new rule set will be created.
     *
     * @param cardType Type of card.
     * @return Corresponding RuleSet for the given card.
     * @throws InvocationTargetException If the rule set could not be created.
     */
    private RuleSet getRuleSetForCardType(CardType cardType) throws InvocationTargetException {
        if (!mappingStore.containsKey(cardType)) {
            mappingStore.put(cardType, RuleSet.createRuleSet(cardType));
        }
        return mappingStore.get(cardType);
    }


    public List<Positionable> simulateAction(ActionCommand commandInfo) throws InvocationTargetException {
        return getRuleSetForCardType(commandInfo.getCard()).simulate(commandInfo.getGame(), commandInfo.getCurrentPlayer());
    }

    public List<Positionable> executeAction(ActionCommand commandInfo) throws InvocationTargetException {
        RuleSet rs = getRuleSetForCardType(commandInfo.getCard());
        List<Positionable> result = rs.execute(commandInfo);
        for (Positionable pos : result = rs.execute(commandInfo)) {
            if (pos instanceof Player) {
                logger.debug(((Player) pos).toString());
            } else if (pos instanceof Loot) {
                logger.debug(((Loot) pos).toString());
            } else {
                logger.warn("Unknown positionable object (no palyer/loot). It will be returned without saving.");
            }
        }

        return result;
    }
}