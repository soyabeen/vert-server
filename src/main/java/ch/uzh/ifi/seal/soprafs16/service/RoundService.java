package ch.uzh.ifi.seal.soprafs16.service;

import ch.uzh.ifi.seal.soprafs16.constant.CardType;
import ch.uzh.ifi.seal.soprafs16.constant.Character;
import ch.uzh.ifi.seal.soprafs16.constant.Turn;
import ch.uzh.ifi.seal.soprafs16.dto.TurnDTO;
import ch.uzh.ifi.seal.soprafs16.model.*;
import ch.uzh.ifi.seal.soprafs16.model.repositories.GameRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.PlayerRepository;
import ch.uzh.ifi.seal.soprafs16.model.repositories.RoundRepository;
import ch.uzh.ifi.seal.soprafs16.utils.InputArgValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by soyabeen on 26.03.16.
 */
@Service("gameRoundService")
public class RoundService {

    private static final Logger logger = LoggerFactory.getLogger(RoundService.class);

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private RoundRepository roundRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private PhaseLogicService logicService;


    /**
     * Retrieves a round chosen by its belonging to a game and its position.
     *
     * @param gameId
     * @param nthRound
     * @return The round object.
     */
    public Round getRoundByGameIdAndRoundNr(Long gameId, Integer nthRound) {
        logger.debug("getRoundByGameIdAndRoundNr with gameId: {} nthRound: {}", gameId, nthRound);

        InputArgValidator.checkIfIdGreaterZero(gameId, "gameid");
        InputArgValidator.checkIfIdGreaterZero(nthRound, "ntRound");
        Game game = (Game) InputArgValidator.checkAvailabeId(gameId, gameRepo, "gameid");

        return roundRepo.findByGameIdAndNthRound(game.getId(), nthRound);
    }

    /**
     * Retrieves a list of turns belonging to a game and a chosen round.
     *
     * @param gameId
     * @param nthRound
     * @return Desired list of Turns.
     */
    public List<Turn> listTurnsForRound(Long gameId, Integer nthRound) {
        logger.debug("listTurnsForRound with gameId: {} nthRound: {}", gameId, nthRound);
        Round round = getRoundByGameIdAndRoundNr(gameId, nthRound);
        return round.getTurns();
    }

    /**
     * Executes chosen action of Player.
     *
     * @param gameId Identifier of game
     * @param move   Action chosen by player
     * @return turnId Nth-turn of player
     */
    public String makeAMove(Long gameId, Integer nthRound, Move move) {
        // holds playerId to lookup player in repo
        Long tmpId;

        InputArgValidator.checkIfIdGreaterZero(nthRound, "ntRound");
        Game game = gameRepo.findOne(gameId);
        InputArgValidator.checkIfGameIsInPlanningPhase(game);
        InputArgValidator.checkCorrectRound(game, nthRound);

        logger.debug("-> Play card {}", move.toString());


        move.setGame(game);
        Round round = roundRepo.findByGameIdAndNthRound(game.getId(), nthRound);

        Player currentPlayer;

        // get owner of card (= player) to access players hand
        tmpId = move.getPlayer().getId();
        currentPlayer = playerRepo.findOne(tmpId);


        if (!move.isPass()) {
            // Player played a card
            playACard(round, move.getPlayedCard());

        } else {
            // Player passed
            passAndTake3(round, currentPlayer);

        }

        // Advance current Player to the next Player
        logicService.advancePlayer(gameId, nthRound);

        // return turnId for player (where turnId is nth-move of player)
        return String.valueOf(currentPlayer.getTotalMadeMoves());
    }

    public Move getMoveFromDTO(Long gameId, String userToken, TurnDTO turnDTO) {

        InputArgValidator.checkAvailabeId(gameId, gameRepo, "MakeMove in Planningphase gamecheck");
        InputArgValidator.checkTokenHasValidPlayer(userToken, playerRepo, "MakeMove in Planningphase playercheck");
        Player player = playerRepo.findByToken(userToken);
        Game game = gameRepo.findOne(gameId);

        logger.error("players id " + player.getId() + " games id " + game.getId());
        InputArgValidator.checkItIsPlayersTurn(player, game);

        Card card = InputArgValidator.checkIfSuchCardOnHand(turnDTO.getType(), player);

        Move move = new Move();

        move.setGame(game);
        move.setPlayer(player);
        move.setPlayedCard(card);

        if (card.getType().equals(CardType.DRAW)) move.setPass(true);

        return move;

    }

    /**
     * Saves played card in card stack.
     *
     * @param playedCard
     * @param round
     * @return round
     */
    protected Round playACard(Round round, Card playedCard) {

        logger.debug("Player {} plays card {} (game:{}, round:{})", playedCard.getOwnerId(), playedCard.getType(), round.getGameId(), round.getNthRound());

        // is it a hidden turn
        playedCard = setFaceDown(round.getTurns(), round.getCardStack().size(), round.getGameId(), playedCard);

        // add played card to card stack
        round.addNewlyPlayedCard(playedCard);

        Player player = playerRepo.findOne(playedCard.getOwnerId());
        player.incrementTotalMadeMoves();
        // remove Card from player hand
        removeCardFromHand(player, playedCard);


        return roundRepo.save(round);
    }

    protected void removeCardFromHand(Player currentPlayer, Card playedCard) {

        if (currentPlayer.getHand().size() != 0) {
            currentPlayer.removeCardFromHand(playedCard);
            // save new hand
            playerRepo.save(currentPlayer);
        }
    }

    /**
     * Passes the turn and adds 3 cards into players hand
     *
     * @param currentPlayer
     */
    protected void passAndTake3(Round round, Player currentPlayer) {
        logger.debug("player {} has passed (game:{}, round:{}", currentPlayer.getUsername(), round.getGameId(), round.getNthRound());
        round.addNewlyPlayedCard(new Card(CardType.DRAW, currentPlayer.getId()));

        //Cards can be not on hand but in deck
        if(round.getCardStack().size() == 0) {
            currentPlayer.take3Cards();
        } else {
            currentPlayer.take3Cards(drawCards(round, currentPlayer));
        }
        currentPlayer.incrementTotalMadeMoves();
        playerRepo.save(currentPlayer);
        roundRepo.save(round);
    }

    private Card setFaceDown(List<Turn> turns, int stackSize, Long gameId, Card playedCard) {
        int turnId = gameRepo.findOne(gameId).getTurnId();
        if (turnId == 1 && playerRepo.findOne(playedCard.getOwnerId()).getCharacter().equals(Character.GHOST)) {
            playedCard.setFaceDown(true);
            return playedCard;
        } else if (!turns.contains(Turn.HIDDEN)) {
            playedCard.setFaceDown(false);
            return playedCard;
        }
        //find what turn number is hidden
        int i = 1;
        List<Integer> turnNumberHidden = new ArrayList<>();
        for (Turn t : turns) {
            if (t.equals(Turn.HIDDEN)) turnNumberHidden.add(i++);
            else if (t.equals(Turn.DOUBLE_TURNS)) i += 2;
            else ++i;
        }
        //is this turn hidden?
        if(turnNumberHidden.contains(turnId)) {
            playedCard.setFaceDown(true);
            return playedCard;
        } else {
            playedCard.setFaceDown(false);
            return playedCard;
        }

    }

    public Round getCurrentRoundInformation(Long gameid) {
        Game game = (Game) InputArgValidator.checkAvailabeId(gameid, gameRepo, "gameId");
        return getRoundByGameIdAndRoundNr(game.getId(), game.getRoundId());
    }

    private List<Card> drawCards(Round round, Player player) {
        List<Card> currentlyNotOnHand = player.getCardsInDeck();
        List<Long> ids = new ArrayList<>();

        for(Card c: currentlyNotOnHand) {
            logger.error("Before" + c.toString());
        }

        for (Card c: round.getCardStack()) ids.add(c.getId());

        for (int i = 0; i < currentlyNotOnHand.size(); ++i) {
            if (ids.contains(currentlyNotOnHand.get(i).getId()))
                currentlyNotOnHand.remove(i);
        }

        for(Card c: currentlyNotOnHand) {
            logger.error("After" + c.toString());
        }

        int numOfCards = 3;
        numOfCards = (numOfCards > currentlyNotOnHand.size()) ? currentlyNotOnHand.size() : numOfCards;

        Collections.shuffle(currentlyNotOnHand);

        List<Card> res = new ArrayList<>();
        for(int i = 0; i < numOfCards; i++) {
            res.add(currentlyNotOnHand.get(i));
        }

        return res;
    }

}
