package SplitWise.Split;

import SplitWise.Models.Transaction;
import lombok.Getter;

import java.util.List;

public class EqualSplitStrategy implements SplitStrategy {
    private final SplitStrategyType splitStrategyType= SplitStrategyType.EQUAL;

    @Override
    public SplitStrategyType getStrategyType() {
        return splitStrategyType;
    }

    @Override
    public List<Transaction> split(Expense expense) {
        return List.of();
    }
}
