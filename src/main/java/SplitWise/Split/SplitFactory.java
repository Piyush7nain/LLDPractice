package SplitWise.Split;

import SplitWise.Models.Split;

import java.util.List;

public class SplitFactory {
    public static SplitStrategy createSplit(List<Split> splits, SplitStrategyType splitStrategyType){
        SplitStrategy splitStrategy = null;
        switch (splitStrategyType){
            case EQUAL -> {
                return new EqualSplitStrategy();
            }
            case EXACT -> {
                return new ExactSplitStrategy(splits);
            }
            case PERCENTAGE -> {
                return new PercentageSplitStragtegy(splits);
            }
            default -> {
                throw new IllegalArgumentException("Unsupported splitStrategyType");
            }
        }

    }
}
