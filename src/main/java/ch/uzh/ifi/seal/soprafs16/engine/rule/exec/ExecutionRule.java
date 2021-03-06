package ch.uzh.ifi.seal.soprafs16.engine.rule.exec;

import ch.uzh.ifi.seal.soprafs16.engine.ActionCommand;
import ch.uzh.ifi.seal.soprafs16.model.Positionable;

import java.util.List;

/**
 * Created by soyabeen on 20.04.16.
 */
public interface ExecutionRule {

    /**
     * Checks the rule set/restriction, to know if this rule can be applied.
     *
     * @return boolean True if the situation satisfy the rule set,
     * so that the action can be executed.
     */
    public boolean evaluate(ActionCommand command);


    /**
     * Execute the rule
     */
    public List<Positionable> execute(ActionCommand command);
}
