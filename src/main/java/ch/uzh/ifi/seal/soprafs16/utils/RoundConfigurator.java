package ch.uzh.ifi.seal.soprafs16.utils;

import ch.uzh.ifi.seal.soprafs16.constant.RoundEndEvent;
import ch.uzh.ifi.seal.soprafs16.constant.Turn;
import ch.uzh.ifi.seal.soprafs16.model.Game;
import ch.uzh.ifi.seal.soprafs16.model.Round;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by soyabeen on 29.03.16.
 */
public class RoundConfigurator {

    public static final int MAX_ROUNDS_FOR_GAME = 4;
    private static final Logger logger = LoggerFactory.getLogger(RoundConfigurator.class);
    private List<RoundConfiguration> roundConfigs;

    private List<RoundConfiguration> stationConfigs;

    public RoundConfigurator() {
        initConfigurations();
    }

    /**
     * Define the round configurations for the 2-4 player round cards and the train station cards.
     */
    private void initConfigurations() {
        roundConfigs = new ArrayList<>();
        roundConfigs.add(new RoundConfiguration(1, new Turn[]{Turn.NORMAL, Turn.DOUBLE_TURNS, Turn.NORMAL}, RoundEndEvent.NONE, "ndn"));
        roundConfigs.add(new RoundConfiguration(2, new Turn[]{Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL, Turn.NORMAL}, RoundEndEvent.PIVOTTABLE_POLE, "nhnn"));
        roundConfigs.add(new RoundConfiguration(3, new Turn[]{Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL}, RoundEndEvent.NONE, "nhnhn"));
        roundConfigs.add(new RoundConfiguration(4, new Turn[]{Turn.NORMAL, Turn.NORMAL, Turn.HIDDEN, Turn.REVERSE}, RoundEndEvent.ANGRY_MARSHAL, "nnhr"));
        roundConfigs.add(new RoundConfiguration(5, new Turn[]{Turn.NORMAL, Turn.NORMAL, Turn.NORMAL, Turn.NORMAL}, RoundEndEvent.BRAKING, "nnnn"));
        roundConfigs.add(new RoundConfiguration(6, new Turn[]{Turn.NORMAL, Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL, Turn.NORMAL}, RoundEndEvent.REBELLION, "nnhnn"));
        roundConfigs.add(new RoundConfiguration(7, new Turn[]{Turn.NORMAL, Turn.HIDDEN, Turn.DOUBLE_TURNS, Turn.NORMAL}, RoundEndEvent.GET_IT_ALL, "nhdn"));
        logger.debug("Added {} RoundConfigurations for 2-4 players.", roundConfigs.size());

        stationConfigs = new ArrayList<>();
        stationConfigs.add(new RoundConfiguration(1, new Turn[]{Turn.NORMAL, Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL}, RoundEndEvent.PICKPOCKETING, "1"));
        stationConfigs.add(new RoundConfiguration(2, new Turn[]{Turn.NORMAL, Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL}, RoundEndEvent.MARSHALS_REVENGE, "2"));
        stationConfigs.add(new RoundConfiguration(3, new Turn[]{Turn.NORMAL, Turn.NORMAL, Turn.HIDDEN, Turn.NORMAL}, RoundEndEvent.HOSTAGE, "3"));
        logger.debug("Added {} RoundConfigurations for train station cards.", roundConfigs.size());
    }

    protected Round buildRoundWithConfig(Game game, Integer position, RoundConfiguration config) {
        return new Round(game.getId(), position, new LinkedList<Turn>(Arrays.asList(config.turns)), config.event, config.getImageType());
    }

    /**
     * Generate number (<code>MAX_ROUNDS_FOR_GAME</code> cards + one station card) of rounds for a game, using the
     * pre configured round definitions -> <code>ch.uzh.ifi.seal.soprafs16.utils.RoundConfigurator.RoundConfiguration</code>.
     *
     * @param game The game for the generated rounds.
     * @return List of generated round objects.
     * @throws IllegalArgumentException if invalid round configurations exists.
     */
    public LinkedList<Round> generateRoundsForGame(Game game) {
        LinkedList<Round> rounds = new LinkedList<>();

        if (roundConfigs == null || MAX_ROUNDS_FOR_GAME > roundConfigs.size()) {
            throw new IllegalStateException("More rounds in MAX_ROUNDS_FOR_GAME than actual round configurations.");
        }
        if (stationConfigs == null || stationConfigs.size() == 0) {
            throw new IllegalStateException("No configurations for train station cards defined.");
        }

        Collections.shuffle(roundConfigs);
        Collections.shuffle(stationConfigs);

        for (int i = 0; i < MAX_ROUNDS_FOR_GAME; i++) {
            logger.debug("Create round with round configuration id {} at position {}", roundConfigs.get(i).getRcId(), i + 1);
            rounds.add(buildRoundWithConfig(game, Integer.valueOf(i + 1), roundConfigs.get(i)));
        }
        logger.debug("Create train station with round configuration id {} at position {}", stationConfigs.get(0).getRcId(), MAX_ROUNDS_FOR_GAME + 1);
        rounds.add(buildRoundWithConfig(game, Integer.valueOf(MAX_ROUNDS_FOR_GAME + 1), stationConfigs.get(0)));

        return rounds;
    }

    protected List<RoundConfiguration> getStationConfigurations() {
        return stationConfigs;
    }

    protected class RoundConfiguration {

        private int rcId;
        private Turn[] turns;
        private RoundEndEvent event;
        private String imageType;

        public RoundConfiguration(int rcId, Turn[] turns, RoundEndEvent event, String img) {
            this.rcId = rcId;
            this.turns = turns;
            this.event = event;
            this.imageType = img;
        }

        public int getRcId() {
            return rcId;
        }

        public Turn[] getTurns() {
            return turns;
        }

        public RoundEndEvent getEvent() {
            return event;
        }

        public String getImageType() {
            return imageType;
        }
    }

}
