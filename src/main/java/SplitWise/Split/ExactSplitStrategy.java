package SplitWise.Split;

import SplitWise.Models.Split;
import SplitWise.Models.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class ExactSplitStrategy  implements SplitStrategy {
    private final SplitStrategyType splitStrategyType= SplitStrategyType.EXACT;

    private final List<Split> splits;
    @Override
    public List<Transaction> split(Expense expense) {
        return List.of();
    }
    @Override
    public SplitStrategyType getStrategyType() {
        return splitStrategyType;
    }
}
